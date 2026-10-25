package by.vadarod.autoparts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class ProductDto {

    private Long id;

    @NotBlank(message = "Наименование обязательно")
    private String name;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.00", message = "Цена не может быть отрицательной")
    private BigDecimal price;

    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество на складе не может быть отрицательным")
    private Integer stockQuantity;
}
