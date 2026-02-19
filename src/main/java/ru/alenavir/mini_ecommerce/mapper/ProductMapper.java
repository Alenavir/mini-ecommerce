package ru.alenavir.mini_ecommerce.mapper;

import org.mapstruct.Mapper;
import ru.alenavir.mini_ecommerce.dto.ProductResponseDto;
import ru.alenavir.mini_ecommerce.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(ProductResponseDto dto);
    ProductResponseDto toDto(Product product);
}
