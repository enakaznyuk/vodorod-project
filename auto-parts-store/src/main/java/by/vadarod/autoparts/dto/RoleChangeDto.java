package by.vadarod.autoparts.dto;

import by.vadarod.autoparts.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeDto {

    @NotNull(message = "Роль обязательна")
    private Role role;
}
