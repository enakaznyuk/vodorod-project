package by.vadarod.receipts.service;

import by.vadarod.receipts.dto.OrderReceiptDto;
import by.vadarod.receipts.dto.OrderReceiptLineDto;
import by.vadarod.receipts.entity.OrderReceipt;
import by.vadarod.receipts.entity.OrderReceiptLine;
import by.vadarod.receipts.exception.ResourceNotFoundException;
import by.vadarod.receipts.kafka.OrderEventItemMessage;
import by.vadarod.receipts.kafka.OrderEventMessage;
import by.vadarod.receipts.kafka.OrderEventTags;
import by.vadarod.receipts.repository.OrderReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderReceiptService {

    private final OrderReceiptRepository orderReceiptRepository;

    public void apply(OrderEventMessage message) {
        if (message.getTag() == null || message.getOrderId() == null) {
            log.warn("Сообщение заказа без тега или номера");
            return;
        }
        switch (message.getTag()) {
            case OrderEventTags.CREATE -> upsert(message);
            case OrderEventTags.UPDATE -> upsert(message);
            case OrderEventTags.DELETE -> delete(message.getOrderId());
            default -> log.warn("Неизвестный тег заказа: {}", message.getTag());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderReceiptDto> findAll() {
        return orderReceiptRepository.findAllByOrderByIdAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderReceiptDto findByOrderId(Long orderId) {
        return toDto(orderReceiptRepository.findBySourceOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Чек заказа не найден: " + orderId)));
    }

    private void upsert(OrderEventMessage message) {
        OrderReceipt receipt = orderReceiptRepository.findBySourceOrderId(message.getOrderId())
                .orElseGet(OrderReceipt::new);
        receipt.setEventId(message.getEventId());
        receipt.setSourceOrderId(message.getOrderId());
        receipt.setUserId(message.getUserId());
        receipt.setFullName(message.getFullName());
        receipt.setPhone(message.getPhone());
        receipt.setEmail(message.getEmail());
        receipt.setOrderDate(message.getOrderDate());
        receipt.setDeliveryAddress(message.getDeliveryAddress());
        receipt.setTotalPrice(money(message.getTotalPrice()));
        receipt.getLines().clear();
        orderReceiptRepository.saveAndFlush(receipt);
        List<OrderEventItemMessage> items = message.getItems() == null ? List.of() : message.getItems();
        for (OrderEventItemMessage item : items) {
            OrderReceiptLine line = new OrderReceiptLine();
            line.setReceipt(receipt);
            line.setProductId(item.getProductId());
            line.setProductName(item.getProductName());
            line.setQuantity(item.getQuantity());
            line.setPrice(money(item.getPrice()));
            line.setLineTotal(money(item.getLineTotal()));
            receipt.getLines().add(line);
        }
        orderReceiptRepository.save(receipt);
        log.info("Чек заказа {} сохранён, тег {}", message.getOrderId(), message.getTag());
    }

    private void delete(Long orderId) {
        orderReceiptRepository.findBySourceOrderId(orderId).ifPresent(receipt -> {
            orderReceiptRepository.delete(receipt);
            log.info("Чек заказа {} удалён", orderId);
        });
    }

    private OrderReceiptDto toDto(OrderReceipt receipt) {
        return OrderReceiptDto.builder()
                .id(receipt.getId())
                .orderId(receipt.getSourceOrderId())
                .userId(receipt.getUserId())
                .fullName(receipt.getFullName())
                .phone(receipt.getPhone())
                .email(receipt.getEmail())
                .orderDate(receipt.getOrderDate())
                .deliveryAddress(receipt.getDeliveryAddress())
                .totalPrice(receipt.getTotalPrice())
                .lines(receipt.getLines().stream()
                        .sorted(Comparator.comparing(OrderReceiptLine::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(this::toLineDto)
                        .toList())
                .build();
    }

    private OrderReceiptLineDto toLineDto(OrderReceiptLine line) {
        return OrderReceiptLineDto.builder()
                .id(line.getId())
                .productId(line.getProductId())
                .productName(line.getProductName())
                .quantity(line.getQuantity())
                .price(line.getPrice())
                .lineTotal(line.getLineTotal())
                .build();
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
