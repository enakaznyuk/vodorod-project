package by.vadarod.autoparts.controller;

import by.vadarod.autoparts.dto.AuthResponse;
import by.vadarod.autoparts.dto.AuthenticationRequest;
import by.vadarod.autoparts.dto.RegistrationRequest;
import by.vadarod.autoparts.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> registration(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registration(request));
    }

    @PostMapping("/authentication")
    public AuthResponse authentication(@Valid @RequestBody AuthenticationRequest request) {
        return authService.authentication(request);
    }
}
