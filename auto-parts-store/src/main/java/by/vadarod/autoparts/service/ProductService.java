package by.vadarod.autoparts.service;

import by.vadarod.autoparts.dto.ProductDto;
import by.vadarod.autoparts.dto.ProductPriceDto;
import by.vadarod.autoparts.entity.Product;
import by.vadarod.autoparts.exception.ConflictException;
import by.vadarod.autoparts.exception.ResourceNotFoundException;
import by.vadarod.autoparts.mapper.StoreMapper;
import by.vadarod.autoparts.repository.CartItemRepository;
import by.vadarod.autoparts.repository.OrderItemRepository;
import by.vadarod.autoparts.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreMapper storeMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto create(ProductDto dto) {
        Product product = new Product();
        apply(product, dto);
        return storeMapper.toProductDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAllByOrderByIdAsc().stream()
                .map(storeMapper::toProductDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return storeMapper.toProductDto(getProduct(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto update(Long id, ProductDto dto) {
        Product product = getProduct(id);
        product.setName(dto.getName().trim());
        product.setStockQuantity(dto.getStockQuantity());
        return storeMapper.toProductDto(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto changePrice(Long id, ProductPriceDto dto) {
        Product product = getProduct(id);
        product.setPrice(dto.getPrice().setScale(2, RoundingMode.HALF_UP));
        return storeMapper.toProductDto(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Товар не найден: " + id);
        }
        if (cartItemRepository.existsByProduct_Id(id) || orderItemRepository.existsByProduct_Id(id)) {
            throw new ConflictException("Товар используется в корзине или заказе и не может быть удалён");
        }
        productRepository.deleteById(id);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар не найден: " + id));
    }

    private void apply(Product product, ProductDto dto) {
        product.setName(dto.getName().trim());
        product.setPrice(dto.getPrice().setScale(2, RoundingMode.HALF_UP));
        product.setStockQuantity(dto.getStockQuantity());
    }
}
