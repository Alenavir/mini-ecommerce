package ru.alenavir.mini_ecommerce.mapper;

import org.mapstruct.*;
import ru.alenavir.mini_ecommerce.dto.order.*;
import ru.alenavir.mini_ecommerce.entity.OrderItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponseDto toDto(OrderItem item);

    List<OrderItemResponseDto> toDtoList(List<OrderItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "price", ignore = true)
    OrderItem toEntity(OrderItemCreateDto dto);
}