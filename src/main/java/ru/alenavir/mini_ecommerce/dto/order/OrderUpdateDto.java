package ru.alenavir.mini_ecommerce.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;

@Getter
@Setter
public class OrderUpdateDto {

    @Size(min = 2, max = 255)
    private String shippingAddress;
}
