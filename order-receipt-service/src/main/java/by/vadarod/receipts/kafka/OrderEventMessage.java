package by.vadarod.receipts.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {

    private String eventId;
    private String tag;
    private Long orderId;
    private Long userId;
    private String fullName;
    private String phone;
    private String email;
    private LocalDateTime orderDate;
    private String deliveryAddress;
    private BigDecimal totalPrice;
    private List<OrderEventItemMessage> items;
}
