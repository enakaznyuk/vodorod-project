package by.vadarod.autoparts.repository;

import by.vadarod.autoparts.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByOrderByIdAsc();

    List<CustomerOrder> findAllByUser_IdOrderByIdAsc(Long userId);

    @Query("select o.user.id from CustomerOrder o where o.id = :id")
    Optional<Long> findOwnerIdById(@Param("id") Long id);
}
