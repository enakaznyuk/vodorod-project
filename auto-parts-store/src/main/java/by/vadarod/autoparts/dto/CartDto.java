package by.vadarod.autoparts.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private Long id;

    @NotNull(message = "Пользователь обязателен")
    private Long userId;

    @Valid
    private List<CartItemDto> items;
}
