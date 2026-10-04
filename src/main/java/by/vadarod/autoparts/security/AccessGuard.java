package by.vadarod.autoparts.security;

import by.vadarod.autoparts.entity.Role;
import by.vadarod.autoparts.exception.UnauthorizedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccessGuard {

    public AuthUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            throw new UnauthorizedException("Требуется авторизация");
        }
        return user;
    }

    public boolean isAdmin() {
        return current().role() == Role.ADMIN;
    }

    public void checkAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Недостаточно прав");
        }
    }

    public void checkSelfOrAdmin(Long userId) {
        AuthUser user = current();
        if (user.role() != Role.ADMIN && !user.id().equals(userId)) {
            throw new AccessDeniedException("Недостаточно прав");
        }
    }
}
