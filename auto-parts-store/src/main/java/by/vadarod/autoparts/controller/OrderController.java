package by.vadarod.autoparts.controller;

import by.vadarod.autoparts.dto.OrderDto;
import by.vadarod.autoparts.dto.OrderItemDto;
import by.vadarod.autoparts.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Заказы")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Создать заказ. Цена берётся из каталога, чек уходит в Kafka")
    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(dto));
    }

    @Operation(summary = "Заказы: свои для пользователя, все для админа")
    @GetMapping
    public List<OrderDto> findAll() {
        return orderService.findAll();
    }

    @Operation(summary = "Заказы пользователя")
    @GetMapping("/user/{userId}")
    public List<OrderDto> findByUserId(@PathVariable("userId") Long userId) {
        return orderService.findByUserId(userId);
    }

    @Operation(summary = "Заказ по номеру")
    @GetMapping("/{id}")
    public OrderDto findById(@PathVariable("id") Long id) {
        return orderService.findById(id);
    }

    @Operation(summary = "Изменить заказ и отправить чек с тегом изменение")
    @PutMapping("/{id}")
    public OrderDto update(@PathVariable("id") Long id, @Valid @RequestBody OrderDto dto) {
        return orderService.update(id, dto);
    }

    @Operation(summary = "Удалить заказ, вернуть товар на склад и отправить тег удаление")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить позицию. Повтор того же товара увеличивает количество")
    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemDto> addItem(@PathVariable("orderId") Long orderId,
                                                 @Valid @RequestBody OrderItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addItem(orderId, dto));
    }

    @Operation(summary = "Позиции заказа")
    @GetMapping("/{orderId}/items")
    public List<OrderItemDto> findItems(@PathVariable("orderId") Long orderId) {
        return orderService.findItems(orderId);
    }

    @Operation(summary = "Одна позиция заказа")
    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItemDto findItem(@PathVariable("orderId") Long orderId,
                                 @PathVariable("itemId") Long itemId) {
        return orderService.findItem(orderId, itemId);
    }

    @Operation(summary = "Изменить позицию заказа")
    @PutMapping("/{orderId}/items/{itemId}")
    public OrderItemDto updateItem(@PathVariable("orderId") Long orderId,
                                   @PathVariable("itemId") Long itemId,
                                   @Valid @RequestBody OrderItemDto dto) {
        return orderService.updateItem(orderId, itemId, dto);
    }

    @Operation(summary = "Удалить позицию и вернуть её количество на склад")
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("orderId") Long orderId,
                                            @PathVariable("itemId") Long itemId) {
        orderService.deleteItem(orderId, itemId);
        return ResponseEntity.noContent().build();
    }
}
