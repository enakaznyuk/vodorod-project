package by.vadarod.autoparts.service;

import by.vadarod.autoparts.dto.AuthResponse;
import by.vadarod.autoparts.dto.AuthenticationRequest;
import by.vadarod.autoparts.dto.RegistrationRequest;
import by.vadarod.autoparts.entity.User;
import by.vadarod.autoparts.exception.UnauthorizedException;
import by.vadarod.autoparts.repository.UserRepository;
import by.vadarod.autoparts.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registration(RegistrationRequest request) {
        return token(userService.registration(request));
    }

    @Transactional(readOnly = true)
    public AuthResponse authentication(AuthenticationRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .filter(found -> passwordEncoder.matches(request.getPassword(), found.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Неверный email или пароль"));
        return token(user);
    }

    private AuthResponse token(User user) {
        return AuthResponse.builder()
                .token(jwtService.generate(user))
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
