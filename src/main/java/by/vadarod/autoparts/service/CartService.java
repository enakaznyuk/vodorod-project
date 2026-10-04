package by.vadarod.autoparts.service;

import by.vadarod.autoparts.dto.CartDto;
import by.vadarod.autoparts.dto.CartItemDto;
import by.vadarod.autoparts.entity.Cart;
import by.vadarod.autoparts.entity.CartItem;
import by.vadarod.autoparts.entity.Product;
import by.vadarod.autoparts.entity.User;
import by.vadarod.autoparts.exception.ConflictException;
import by.vadarod.autoparts.exception.ResourceNotFoundException;
import by.vadarod.autoparts.mapper.StoreMapper;
import by.vadarod.autoparts.repository.CartRepository;
import by.vadarod.autoparts.repository.ProductRepository;
import by.vadarod.autoparts.repository.UserRepository;
import by.vadarod.autoparts.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StoreMapper storeMapper;
    private final AccessGuard accessGuard;

    public CartDto create(CartDto dto) {
        accessGuard.checkSelfOrAdmin(dto.getUserId());
        if (cartRepository.existsByUser_Id(dto.getUserId())) {
            throw new ConflictException("У пользователя уже есть корзина");
        }
        Cart cart = new Cart();
        cart.setUser(getUser(dto.getUserId()));
        addItems(cart, dto.getItems());
        return storeMapper.toCartDto(cartRepository.saveAndFlush(cart));
    }

    @Transactional(readOnly = true)
    public List<CartDto> findAll() {
        if (accessGuard.isAdmin()) {
            return cartRepository.findAllByOrderByIdAsc().stream()
                    .map(storeMapper::toCartDto)
                    .toList();
        }
        return cartRepository.findByUser_Id(accessGuard.current().id())
                .map(storeMapper::toCartDto)
                .stream()
                .toList();
    }

    @Transactional(readOnly = true)
    public CartDto findById(Long id) {
        return storeMapper.toCartDto(requireCart(id));
    }

    @Transactional(readOnly = true)
    public CartDto findByUserId(Long userId) {
        accessGuard.checkSelfOrAdmin(userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь не найден: " + userId);
        }
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Корзина пользователя не найдена: " + userId));
        return storeMapper.toCartDto(cart);
    }

    public CartDto update(Long id, CartDto dto) {
        Cart cart = requireCart(id);
        if (!cart.getUser().getId().equals(dto.getUserId())) {
            accessGuard.checkAdmin();
            if (cartRepository.existsByUser_Id(dto.getUserId())) {
                throw new ConflictException("У пользователя уже есть корзина");
            }
            cart.setUser(getUser(dto.getUserId()));
        }
        if (dto.getItems() != null) {
            cart.getItems().clear();
            cartRepository.flush();
            addItems(cart, dto.getItems());
        }
        return storeMapper.toCartDto(cartRepository.saveAndFlush(cart));
    }

    public void delete(Long id) {
        cartRepository.delete(requireCart(id));
    }

    public CartItemDto addItem(Long cartId, CartItemDto dto) {
        Cart cart = requireCart(cartId);
        ensureProductIsFree(cart, dto.getProductId(), null);
        CartItem item = toItem(cart, dto);
        cart.getItems().add(item);
        cartRepository.saveAndFlush(cart);
        return storeMapper.toCartItemDto(item);
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> findItems(Long cartId) {
        return storeMapper.toCartDto(requireCart(cartId)).getItems();
    }

    @Transactional(readOnly = true)
    public CartItemDto findItem(Long cartId, Long itemId) {
        return storeMapper.toCartItemDto(getItem(requireCart(cartId), itemId));
    }

    public CartItemDto updateItem(Long cartId, Long itemId, CartItemDto dto) {
        Cart cart = requireCart(cartId);
        CartItem item = getItem(cart, itemId);
        ensureProductIsFree(cart, dto.getProductId(), itemId);
        Product product = getProduct(dto.getProductId());
        item.setProduct(product);
        item.setQuantity(dto.getQuantity());
        item.setPrice(resolvePrice(dto.getPrice(), product.getPrice()));
        cartRepository.saveAndFlush(cart);
        return storeMapper.toCartItemDto(item);
    }

    public void deleteItem(Long cartId, Long itemId) {
        Cart cart = requireCart(cartId);
        getItem(cart, itemId);
        cart.getItems().removeIf(existing -> itemId.equals(existing.getId()));
        cartRepository.saveAndFlush(cart);
    }

    private void addItems(Cart cart, List<CartItemDto> itemDtos) {
        if (itemDtos == null || itemDtos.isEmpty()) {
            return;
        }
        ensureUniqueProducts(itemDtos);
        for (CartItemDto itemDto : itemDtos) {
            cart.getItems().add(toItem(cart, itemDto));
        }
    }

    private CartItem toItem(Cart cart, CartItemDto dto) {
        Product product = getProduct(dto.getProductId());
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(dto.getQuantity());
        item.setPrice(resolvePrice(dto.getPrice(), product.getPrice()));
        return item;
    }

    private void ensureUniqueProducts(List<CartItemDto> itemDtos) {
        Set<Long> productIds = new HashSet<>();
        for (CartItemDto itemDto : itemDtos) {
            if (!productIds.add(itemDto.getProductId())) {
                throw new ConflictException("Товар уже добавлен в корзину: " + itemDto.getProductId());
            }
        }
    }

    private void ensureProductIsFree(Cart cart, Long productId, Long currentItemId) {
        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId)
                        && (currentItemId == null || !currentItemId.equals(item.getId())));
        if (exists) {
            throw new ConflictException("Товар уже добавлен в корзину: " + productId);
        }
    }

    private Cart getCart(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Корзина не найдена: " + id));
    }

    private Cart requireCart(Long id) {
        Cart cart = getCart(id);
        accessGuard.checkSelfOrAdmin(cart.getUser().getId());
        return cart;
    }

    private CartItem getItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Позиция корзины не найдена: " + itemId));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар не найден: " + id));
    }

    private BigDecimal resolvePrice(BigDecimal requestedPrice, BigDecimal productPrice) {
        BigDecimal price = requestedPrice != null ? requestedPrice : productPrice;
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
