package ru.alenavir.mini_ecommerce.mapper;

import org.mapstruct.*;
import ru.alenavir.mini_ecommerce.dto.product.ProductCreateDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductResponseDto;
import ru.alenavir.mini_ecommerce.dto.product.ProductUpdateDto;
import ru.alenavir.mini_ecommerce.entity.Product;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductCreateDto dto);

    ProductResponseDto toResponse(Product product);

    List<ProductResponseDto> toResponseList(List<Product> products);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromDto(ProductUpdateDto dto, @MappingTarget Product entity);
}