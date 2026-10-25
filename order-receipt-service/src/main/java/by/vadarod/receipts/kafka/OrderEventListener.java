package by.vadarod.receipts.kafka;

import by.vadarod.receipts.service.OrderReceiptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final OrderReceiptService orderReceiptService;

    @KafkaListener(topics = "${app.kafka.orders-topic}")
    public void listen(String payload) {
        OrderEventMessage message;
        try {
            message = objectMapper.readValue(payload, OrderEventMessage.class);
        } catch (JsonProcessingException exception) {
            log.error("Некорректное сообщение заказа: {}", payload, exception);
            return;
        }
        orderReceiptService.apply(message);
    }
}
