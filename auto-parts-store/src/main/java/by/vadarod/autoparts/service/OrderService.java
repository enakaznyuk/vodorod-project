package by.vadarod.autoparts.service;

import by.vadarod.autoparts.dto.OrderDto;
import by.vadarod.autoparts.dto.OrderItemDto;
import by.vadarod.autoparts.entity.CustomerOrder;
import by.vadarod.autoparts.entity.OrderItem;
import by.vadarod.autoparts.entity.Product;
import by.vadarod.autoparts.entity.Role;
import by.vadarod.autoparts.entity.User;
import by.vadarod.autoparts.exception.ConflictException;
import by.vadarod.autoparts.exception.ResourceNotFoundException;
import by.vadarod.autoparts.kafka.OrderEventPublisher;
import by.vadarod.autoparts.kafka.OrderEventTag;
import by.vadarod.autoparts.mapper.StoreMapper;
import by.vadarod.autoparts.repository.OrderRepository;
import by.vadarod.autoparts.repository.ProductRepository;
import by.vadarod.autoparts.repository.UserRepository;
import by.vadarod.autoparts.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StoreMapper storeMapper;
    private final OrderEventPublisher orderEventPublisher;

    @PreAuthorize("hasRole('ADMIN') or #dto.userId == principal.id()")
    public OrderDto create(OrderDto dto) {
        CustomerOrder order = new CustomerOrder();
        order.setUser(getUser(dto.getUserId()));
        order.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDateTime.now());
        order.setDeliveryAddress(dto.getDeliveryAddress().trim());
        order.setTotalPrice(money(BigDecimal.ZERO));
        addItems(order, dto.getItems());
        recalculateTotal(order);
        CustomerOrder saved = orderRepository.saveAndFlush(order);
        orderEventPublisher.publish(saved, OrderEventTag.CREATE);
        return storeMapper.toOrderDto(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        AuthUser current = currentUser();
        if (current.role() != Role.ADMIN) {
            return orderRepository.findAllByUser_IdOrderByIdAsc(current.id()).stream()
                    .map(storeMapper::toOrderDto)
                    .toList();
        }
        return orderRepository.findAllByOrderByIdAsc().stream()
                .map(storeMapper::toOrderDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.ownsOrder(#id)")
    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return storeMapper.toOrderDto(getOrder(id));
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id()")
    @Transactional(readOnly = true)
    public List<OrderDto> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь не найден: " + userId);
        }
        return orderRepository.findAllByUser_IdOrderByIdAsc(userId).stream()
                .map(storeMapper::toOrderDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or (@resourceAccess.ownsOrder(#id) and #dto.userId == principal.id())")
    public OrderDto update(Long id, OrderDto dto) {
        CustomerOrder order = getOrder(id);
        returnStock(order);
        order.getItems().clear();
        orderRepository.flush();
        order.setUser(getUser(dto.getUserId()));
        if (dto.getOrderDate() != null) {
            order.setOrderDate(dto.getOrderDate());
        }
        order.setDeliveryAddress(dto.getDeliveryAddress().trim());
        addItems(order, dto.getItems());
        recalculateTotal(order);
        CustomerOrder saved = orderRepository.saveAndFlush(order);
        orderEventPublisher.publish(saved, OrderEventTag.UPDATE);
        return storeMapper.toOrderDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.ownsOrder(#id)")
    public void delete(Long id) {
        CustomerOrder order = getOrder(id);
        orderEventPublisher.publish(order, OrderEventTag.DELETE);
        returnStock(order);
        orderRepository.delete(order);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id()")
    public void deleteAllByUserId(Long userId) {
        List<CustomerOrder> orders = orderRepository.findAllByUser_IdOrderByIdAsc(userId);
        for (CustomerOrder order : orders) {
            orderEventPublisher.publish(order, OrderEventTag.DELETE);
            returnStock(order);
            orderRepository.delete(order);
        }
        orderRepository.flush();
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.ownsOrder(#orderId)")
    public OrderItemDto addItem(Long orderId, OrderItemDto dto) {
        CustomerOrder order = getOrder(orderId);
        Product product = getProduct(dto.getProductId());
        OrderItem existing = findItemByProduct(order, product.getId());
        if (existing != null) {
            takeFromStock(product, dto.getQuantity());
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            applyCatalogPrice(existing, product);
            recalculateTotal(order);
            orderRepository.saveAndFlush(order);
            orderEventPublisher.publish(order, OrderEventTag.UPDATE);
            return storeMapper.toOrderItemDto(existing);
        }
        OrderItem item = toItem(order, dto);
        order.getItems().add(item);
        recalculateTotal(order);
        orderRepository.saveAndFlush(order);
        orderEventPublisher.publish(order, OrderEventTag.UPDATE);
        return storeMapper.toOrderItemDto(item);
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.ownsOrder(#orderId)")
    @Transactional(readOnly = true)
    public List<OrderItemDto> findItems(Long orderId) {
        return storeMapper.toOrderDto(getOrder(orderId)).getItems();
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.ownsOrder(#orderId)")
    @Transactional(readOnly = true)
    public OrderItemDto findItem(Long orderId, Long itemId) {
        return storeMapper.toOrderItemDto(getItem(getOrder(orderId), itemId));
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.ownsOrder(#orderId)")
    public OrderItemDto updateItem(Long orderId, Long itemId, OrderItemDto dto) {
        CustomerOrder order = getOrder(orderId);
        OrderItem item = getItem(order, itemId);
        returnStock(item.getProduct(), item.getQuantity());
        Product product = getProduct(dto.getProductId());
        takeFromStock(product, dto.getQuantity());
        OrderItem sameProduct = findItemByProduct(order, product.getId());
        if (sameProduct != null && !sameProduct.getId().equals(itemId)) {
            sameProduct.setQuantity(sameProduct.getQuantity() + dto.getQuantity());
            applyCatalogPrice(sameProduct, product);
            order.getItems().removeIf(existing -> itemId.equals(existing.getId()));
            item = sameProduct;
        } else {
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            applyCatalogPrice(item, product);
        }
        recalculateTotal(order);
        orderRepository.saveAndFlush(order);
        orderEventPublisher.publish(order, OrderEventTag.UPDATE);
        return storeMapper.toOrderItemDto(item);
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.ownsOrder(#orderId)")
    public void deleteItem(Long orderId, Long itemId) {
        CustomerOrder order = getOrder(orderId);
        OrderItem item = getItem(order, itemId);
        returnStock(item.getProduct(), item.getQuantity());
        order.getItems().removeIf(existing -> itemId.equals(existing.getId()));
        recalculateTotal(order);
        orderRepository.saveAndFlush(order);
        orderEventPublisher.publish(order, OrderEventTag.UPDATE);
    }

    private void addItems(CustomerOrder order, List<OrderItemDto> itemDtos) {
        for (OrderItemDto itemDto : mergeByProduct(itemDtos)) {
            order.getItems().add(toItem(order, itemDto));
        }
    }

    private List<OrderItemDto> mergeByProduct(List<OrderItemDto> itemDtos) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemDto itemDto : itemDtos) {
            quantities.merge(itemDto.getProductId(), itemDto.getQuantity(), Integer::sum);
        }
        List<OrderItemDto> merged = new ArrayList<>();
        quantities.forEach((productId, quantity) -> merged.add(OrderItemDto.builder()
                .productId(productId)
                .quantity(quantity)
                .build()));
        return merged;
    }

    private OrderItem toItem(CustomerOrder order, OrderItemDto dto) {
        Product product = getProduct(dto.getProductId());
        takeFromStock(product, dto.getQuantity());
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(dto.getQuantity());
        applyCatalogPrice(item, product);
        return item;
    }

    private void applyCatalogPrice(OrderItem item, Product product) {
        item.setProductName(product.getName());
        item.setPrice(money(product.getPrice()));
    }

    private OrderItem findItemByProduct(CustomerOrder order, Long productId) {
        return order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void returnStock(CustomerOrder order) {
        for (OrderItem item : order.getItems()) {
            returnStock(item.getProduct(), item.getQuantity());
        }
    }

    private void takeFromStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new ConflictException("Недостаточно товара «" + product.getName() + "» на складе");
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
    }

    private void returnStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() + quantity);
    }

    private void recalculateTotal(CustomerOrder order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(money(total));
    }

    private CustomerOrder getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден: " + id));
    }

    private AuthUser currentUser() {
        return (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private OrderItem getItem(CustomerOrder order, Long itemId) {
        return order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Позиция заказа не найдена: " + itemId));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар не найден: " + id));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
