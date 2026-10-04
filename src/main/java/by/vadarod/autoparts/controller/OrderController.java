package by.vadarod.autoparts.controller;

import by.vadarod.autoparts.dto.OrderDto;
import by.vadarod.autoparts.dto.OrderItemDto;
import by.vadarod.autoparts.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(dto));
    }

    @GetMapping
    public List<OrderDto> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<OrderDto> findByUserId(@PathVariable("userId") Long userId) {
        return orderService.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public OrderDto findById(@PathVariable("id") Long id) {
        return orderService.findById(id);
    }

    @PutMapping("/{id}")
    public OrderDto update(@PathVariable("id") Long id, @Valid @RequestBody OrderDto dto) {
        return orderService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemDto> addItem(@PathVariable("orderId") Long orderId,
                                                 @Valid @RequestBody OrderItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addItem(orderId, dto));
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItemDto> findItems(@PathVariable("orderId") Long orderId) {
        return orderService.findItems(orderId);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItemDto findItem(@PathVariable("orderId") Long orderId,
                                 @PathVariable("itemId") Long itemId) {
        return orderService.findItem(orderId, itemId);
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public OrderItemDto updateItem(@PathVariable("orderId") Long orderId,
                                   @PathVariable("itemId") Long itemId,
                                   @Valid @RequestBody OrderItemDto dto) {
        return orderService.updateItem(orderId, itemId, dto);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("orderId") Long orderId,
                                            @PathVariable("itemId") Long itemId) {
        orderService.deleteItem(orderId, itemId);
        return ResponseEntity.noContent().build();
    }
}
