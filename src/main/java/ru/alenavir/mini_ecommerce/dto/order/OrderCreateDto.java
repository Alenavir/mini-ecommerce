package ru.alenavir.mini_ecommerce.dto.order;

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
public class OrderCreateDto {

    @NotNull
    private Long userId;

    @NotEmpty
    private List<OrderItemCreateDto> items;

    @NotBlank(message = "ShippingAddress must not be blank")
    @Size(min = 2, max = 255)
    private String shippingAddress;

    @NotNull
    private PaymentMethod paymentMethod;
}