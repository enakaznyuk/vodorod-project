package by.vadarod.autoparts.security;

import by.vadarod.autoparts.entity.Role;

public record AuthUser(Long id, String email, Role role) {
}
