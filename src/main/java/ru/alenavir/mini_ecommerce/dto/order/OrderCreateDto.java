package ru.alenavir.mini_ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.PaymentMethod;

import java.util.List;

@Setter
@Getter
@Schema(description = "DTO для создания заказа")
public class OrderCreateDto {

    @Schema(description = "ID пользователя", example = "1")
    @NotNull
    private Long userId;

    @Schema(description = "Список товаров в заказе")
    @NotEmpty
    private List<OrderItemCreateDto> items;

    @Schema(description = "Адрес доставки", example = "Amsterdam, Main Street 10")
    @NotBlank
    @Size(min = 2, max = 255)
    private String shippingAddress;

    @Schema(description = "Способ оплаты", example = "CARD")
    @NotNull
    private PaymentMethod paymentMethod;
}