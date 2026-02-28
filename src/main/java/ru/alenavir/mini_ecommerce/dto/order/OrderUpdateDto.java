package ru.alenavir.mini_ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO для обновления заказа")
public class OrderUpdateDto {

    @Schema(description = "Новый адрес доставки", example = "Rotterdam, New Street 5")
    @Size(min = 2, max = 255)
    private String shippingAddress;
}
