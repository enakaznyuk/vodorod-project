package by.vadarod.autoparts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    private Long id;

    @NotNull(message = "Товар обязателен")
    private Long productId;

    private String productName;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть не меньше 1")
    private Integer quantity;

    @DecimalMin(value = "0.00", message = "Цена не может быть отрицательной")
    private BigDecimal price;
}
