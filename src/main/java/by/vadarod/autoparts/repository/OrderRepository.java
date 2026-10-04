package by.vadarod.autoparts.repository;

import by.vadarod.autoparts.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByOrderByIdAsc();

    List<CustomerOrder> findAllByUser_IdOrderByIdAsc(Long userId);
}
