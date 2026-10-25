package by.vadarod.autoparts.kafka;

import by.vadarod.autoparts.entity.CustomerOrder;
import by.vadarod.autoparts.entity.OrderItem;
import by.vadarod.autoparts.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.orders-topic}")
    private String topic;

    public void publish(CustomerOrder order, OrderEventTag tag) {
        OrderEventMessage message = toMessage(order, tag);
        String payload = write(message);
        String key = String.valueOf(message.getOrderId());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(key, payload, message.getOrderId(), tag);
                }
            });
            return;
        }
        send(key, payload, message.getOrderId(), tag);
    }

    private void send(String key, String payload, Long orderId, OrderEventTag tag) {
        try {
            kafkaTemplate.send(topic, key, payload).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Не удалось отправить событие {} заказа {}", tag.value(), orderId, exception);
                }
            });
        } catch (RuntimeException exception) {
            log.error("Не удалось отправить событие {} заказа {}", tag.value(), orderId, exception);
        }
    }

    private OrderEventMessage toMessage(CustomerOrder order, OrderEventTag tag) {
        User user = order.getUser();
        return OrderEventMessage.builder()
                .eventId(UUID.randomUUID().toString())
                .tag(tag.value())
                .orderId(order.getId())
                .userId(user.getId())
                .fullName(user.combinedFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .orderDate(order.getOrderDate())
                .deliveryAddress(order.getDeliveryAddress())
                .totalPrice(order.getTotalPrice())
                .items(order.getItems().stream()
                        .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(this::toItem)
                        .toList())
                .build();
    }

    private OrderEventItemMessage toItem(OrderItem item) {
        BigDecimal lineTotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return OrderEventItemMessage.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .lineTotal(lineTotal)
                .build();
    }

    private String write(OrderEventMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Не удалось собрать сообщение заказа", exception);
        }
    }
}
