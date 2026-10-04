package by.vadarod.autoparts.repository;

import by.vadarod.autoparts.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findAllByOrderByIdAsc();

    Optional<Cart> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
}
