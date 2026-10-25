package by.vadarod.autoparts.controller;

import by.vadarod.autoparts.dto.ProductDto;
import by.vadarod.autoparts.dto.ProductPriceDto;
import by.vadarod.autoparts.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Товары")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Создать товар. Только админ")
    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    @Operation(summary = "Список товаров")
    @GetMapping
    public List<ProductDto> findAll() {
        return productService.findAll();
    }

    @Operation(summary = "Товар по номеру")
    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    @Operation(summary = "Изменить название и остаток. Цену не меняет")
    @PutMapping("/{id}")
    public ProductDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductDto dto) {
        return productService.update(id, dto);
    }

    @Operation(summary = "Изменить цену товара. Только админ")
    @PatchMapping("/{id}/price")
    public ProductDto changePrice(@PathVariable("id") Long id, @Valid @RequestBody ProductPriceDto dto) {
        return productService.changePrice(id, dto);
    }

    @Operation(summary = "Удалить товар. Только админ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
