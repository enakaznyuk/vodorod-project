package by.vadarod.receipts.repository;

import by.vadarod.receipts.entity.OrderReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderReceiptRepository extends JpaRepository<OrderReceipt, Long> {

    Optional<OrderReceipt> findBySourceOrderId(Long sourceOrderId);

    List<OrderReceipt> findAllByOrderByIdAsc();
}
