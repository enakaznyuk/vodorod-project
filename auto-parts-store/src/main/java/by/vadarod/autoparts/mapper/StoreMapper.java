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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(target = "fullName", source = ".", qualifiedByName = "fullName")
    @Mapping(target = "password", ignore = true)
    UserDto toUserDto(User user);

    ProductDto toProductDto(Product product);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items", qualifiedByName = "sortedCartItems")
    CartDto toCartDto(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    CartItemDto toCartItemDto(CartItem item);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items", qualifiedByName = "sortedOrderItems")
    OrderDto toOrderDto(CustomerOrder order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "lineTotal", source = ".", qualifiedByName = "lineTotal")
    OrderItemDto toOrderItemDto(OrderItem item);

    @Named("fullName")
    default String fullName(User user) {
        return user.combinedFullName();
    }

    @Named("sortedCartItems")
    default List<CartItemDto> sortedCartItems(List<CartItem> items) {
        return items.stream()
                .sorted(Comparator.comparing(CartItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toCartItemDto)
                .toList();
    }

    @Named("sortedOrderItems")
    default List<OrderItemDto> sortedOrderItems(List<OrderItem> items) {
        return items.stream()
                .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toOrderItemDto)
                .toList();
    }

    @Named("lineTotal")
    default BigDecimal lineTotal(OrderItem item) {
        return item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
