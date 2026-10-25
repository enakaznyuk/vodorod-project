package by.vadarod.autoparts.controller;

import by.vadarod.autoparts.dto.AuthResponse;
import by.vadarod.autoparts.dto.AuthenticationRequest;
import by.vadarod.autoparts.dto.RegistrationRequest;
import by.vadarod.autoparts.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Авторизация")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Регистрация пользователя с ролью USER")
    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> registration(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registration(request));
    }

    @Operation(summary = "Вход по email и паролю, в ответе JWT")
    @PostMapping("/authentication")
    public AuthResponse authentication(@Valid @RequestBody AuthenticationRequest request) {
        return authService.authentication(request);
    }
}
