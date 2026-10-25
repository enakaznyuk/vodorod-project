package by.vadarod.autoparts.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class OrderDto {

    private Long id;

    @NotNull(message = "Пользователь обязателен")
    private Long userId;

    private BigDecimal totalPrice;

    private LocalDateTime orderDate;

    @NotBlank(message = "Адрес доставки обязателен")
    private String deliveryAddress;

    @NotEmpty(message = "В заказе должен быть хотя бы один товар")
    @Valid
    private List<OrderItemDto> items;
}
