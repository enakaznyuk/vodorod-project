package by.vadarod.autoparts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceDto {

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.00", message = "Цена не может быть отрицательной")
    private BigDecimal price;
}
