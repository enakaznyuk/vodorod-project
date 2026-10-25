package by.vadarod.receipts.dto;

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
public class OrderReceiptDto {

    private Long id;
    private Long orderId;
    private Long userId;
    private String fullName;
    private String phone;
    private String email;
    private LocalDateTime orderDate;
    private String deliveryAddress;
    private BigDecimal totalPrice;
    private List<OrderReceiptLineDto> lines;
}
