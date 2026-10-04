package by.vadarod.autoparts.service;

import by.vadarod.autoparts.dto.RegistrationRequest;
import by.vadarod.autoparts.dto.UserDto;
import by.vadarod.autoparts.entity.Role;
import by.vadarod.autoparts.entity.User;
import by.vadarod.autoparts.exception.ConflictException;
import by.vadarod.autoparts.exception.ResourceNotFoundException;
import by.vadarod.autoparts.mapper.StoreMapper;
import by.vadarod.autoparts.repository.CartRepository;
import by.vadarod.autoparts.repository.UserRepository;
import by.vadarod.autoparts.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderService orderService;
    private final StoreMapper storeMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccessGuard accessGuard;

    public User registration(RegistrationRequest request) {
        String email = normalizeEmail(request.getEmail());
        ensureEmailFree(email);
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public UserDto create(UserDto dto) {
        accessGuard.checkAdmin();
        String email = normalizeEmail(dto.getEmail());
        ensureEmailFree(email);
        User user = new User();
        apply(user, dto, email);
        user.setRole(Role.USER);
        return storeMapper.toUserDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        accessGuard.checkAdmin();
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(storeMapper::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        accessGuard.checkSelfOrAdmin(id);
        return storeMapper.toUserDto(getUser(id));
    }

    public UserDto update(Long id, UserDto dto) {
        accessGuard.checkSelfOrAdmin(id);
        User user = getUser(id);
        String email = normalizeEmail(dto.getEmail());
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Пользователь с таким email уже существует");
                });
        apply(user, dto, email);
        return storeMapper.toUserDto(userRepository.save(user));
    }

    public UserDto changeRole(Long id, Role role) {
        accessGuard.checkAdmin();
        User user = getUser(id);
        user.setRole(role);
        return storeMapper.toUserDto(userRepository.save(user));
    }

    public void delete(Long id) {
        accessGuard.checkSelfOrAdmin(id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь не найден: " + id);
        }
        orderService.deleteAllByUserId(id);
        cartRepository.findByUser_Id(id).ifPresent(cartRepository::delete);
        cartRepository.flush();
        userRepository.deleteById(id);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
    }

    private void ensureEmailFree(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
    }

    private void apply(User user, UserDto dto, String email) {
        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
