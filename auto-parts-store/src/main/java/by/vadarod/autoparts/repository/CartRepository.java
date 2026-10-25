package by.vadarod.autoparts.repository;

import by.vadarod.autoparts.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findAllByOrderByIdAsc();

    Optional<Cart> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    @Query("select c.user.id from Cart c where c.id = :id")
    Optional<Long> findOwnerIdById(@Param("id") Long id);
}
