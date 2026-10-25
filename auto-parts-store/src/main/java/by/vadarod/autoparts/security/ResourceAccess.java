package by.vadarod.autoparts.security;

import by.vadarod.autoparts.repository.CartRepository;
import by.vadarod.autoparts.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("resourceAccess")
@RequiredArgsConstructor
public class ResourceAccess {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public boolean ownsOrder(Long orderId) {
        if (orderId == null) {
            return false;
        }
        return orderRepository.findOwnerIdById(orderId)
                .map(ownerId -> ownerId.equals(currentUserId()))
                .orElse(true);
    }

    public boolean ownsCart(Long cartId) {
        if (cartId == null) {
            return false;
        }
        return cartRepository.findOwnerIdById(cartId)
                .map(ownerId -> ownerId.equals(currentUserId()))
                .orElse(true);
    }

    private Long currentUserId() {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.id();
    }
}
