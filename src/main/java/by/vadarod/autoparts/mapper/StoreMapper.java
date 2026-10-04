package by.vadarod.autoparts.mapper;

import by.vadarod.autoparts.dto.CartDto;
import by.vadarod.autoparts.dto.CartItemDto;
import by.vadarod.autoparts.dto.OrderDto;
import by.vadarod.autoparts.dto.OrderItemDto;
import by.vadarod.autoparts.dto.ProductDto;
import by.vadarod.autoparts.dto.UserDto;
import by.vadarod.autoparts.entity.Cart;
import by.vadarod.autoparts.entity.CartItem;
import by.vadarod.autoparts.entity.CustomerOrder;
import by.vadarod.autoparts.entity.OrderItem;
import by.vadarod.autoparts.entity.Product;
import by.vadarod.autoparts.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

@Component
public class StoreMapper {

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    public CartDto toCartDto(Cart cart) {
        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(cart.getItems().stream()
                        .sorted(Comparator.comparing(CartItem::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(this::toCartItemDto)
                        .toList())
                .build();
    }

    public CartItemDto toCartItemDto(CartItem item) {
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }

    public OrderDto toOrderDto(CustomerOrder order) {
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getOrderDate())
                .deliveryAddress(order.getDeliveryAddress())
                .items(order.getItems().stream()
                        .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(this::toOrderItemDto)
                        .toList())
                .build();
    }

    public OrderItemDto toOrderItemDto(OrderItem item) {
        BigDecimal lineTotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .lineTotal(lineTotal)
                .build();
    }
}
