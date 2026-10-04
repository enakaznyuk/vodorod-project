package by.vadarod.autoparts.controller;

import by.vadarod.autoparts.dto.CartDto;
import by.vadarod.autoparts.dto.CartItemDto;
import by.vadarod.autoparts.service.CartService;
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
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartDto> create(@Valid @RequestBody CartDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.create(dto));
    }

    @GetMapping
    public List<CartDto> findAll() {
        return cartService.findAll();
    }

    @GetMapping("/user/{userId}")
    public CartDto findByUserId(@PathVariable("userId") Long userId) {
        return cartService.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public CartDto findById(@PathVariable("id") Long id) {
        return cartService.findById(id);
    }

    @PutMapping("/{id}")
    public CartDto update(@PathVariable("id") Long id, @Valid @RequestBody CartDto dto) {
        return cartService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        cartService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDto> addItem(@PathVariable("cartId") Long cartId,
                                                @Valid @RequestBody CartItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(cartId, dto));
    }

    @GetMapping("/{cartId}/items")
    public List<CartItemDto> findItems(@PathVariable("cartId") Long cartId) {
        return cartService.findItems(cartId);
    }

    @GetMapping("/{cartId}/items/{itemId}")
    public CartItemDto findItem(@PathVariable("cartId") Long cartId, @PathVariable("itemId") Long itemId) {
        return cartService.findItem(cartId, itemId);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public CartItemDto updateItem(@PathVariable("cartId") Long cartId,
                                  @PathVariable("itemId") Long itemId,
                                  @Valid @RequestBody CartItemDto dto) {
        return cartService.updateItem(cartId, itemId, dto);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("cartId") Long cartId,
                                            @PathVariable("itemId") Long itemId) {
        cartService.deleteItem(cartId, itemId);
        return ResponseEntity.noContent().build();
    }
}
