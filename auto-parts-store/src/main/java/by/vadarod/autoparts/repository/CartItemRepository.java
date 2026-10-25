package by.vadarod.autoparts.repository;

import by.vadarod.autoparts.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    boolean existsByProduct_Id(Long productId);
}
