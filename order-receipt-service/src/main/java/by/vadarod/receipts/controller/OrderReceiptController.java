package by.vadarod.receipts.controller;

import by.vadarod.receipts.dto.OrderReceiptDto;
import by.vadarod.receipts.service.OrderReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Чеки")
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class OrderReceiptController {

    private final OrderReceiptService orderReceiptService;

    @Operation(summary = "Все сохранённые чеки")
    @GetMapping
    public List<OrderReceiptDto> findAll() {
        return orderReceiptService.findAll();
    }

    @Operation(summary = "Чек по номеру заказа магазина")
    @GetMapping("/order/{orderId}")
    public OrderReceiptDto findByOrderId(@PathVariable("orderId") Long orderId) {
        return orderReceiptService.findByOrderId(orderId);
    }
}
