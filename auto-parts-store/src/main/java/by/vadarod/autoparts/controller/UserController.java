package by.vadarod.autoparts.controller;

import by.vadarod.autoparts.dto.RoleChangeDto;
import by.vadarod.autoparts.dto.UserDto;
import by.vadarod.autoparts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Пользователи")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Создать пользователя. Только админ, роль в базе будет USER")
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @Operation(summary = "Список всех пользователей. Только админ")
    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @Operation(summary = "Профиль пользователя. Свой профиль или любой для админа")
    @GetMapping("/{id}")
    public UserDto findById(@PathVariable("id") Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "Изменить профиль. Роль из тела запроса не применяется")
    @PutMapping("/{id}")
    public UserDto update(@PathVariable("id") Long id, @Valid @RequestBody UserDto dto) {
        return userService.update(id, dto);
    }

    @Operation(summary = "Назначить роль. Только админ")
    @PatchMapping("/{id}/role")
    public UserDto changeRole(@PathVariable("id") Long id, @Valid @RequestBody RoleChangeDto dto) {
        return userService.changeRole(id, dto.getRole());
    }

    @Operation(summary = "Удалить пользователя вместе с корзиной и заказами")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
