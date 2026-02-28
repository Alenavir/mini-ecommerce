package ru.alenavir.mini_ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.entity.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Ответ с информацией о заказе")
public class OrderResponseDto {

    @Schema(description = "ID заказа", example = "10")
    private Long id;

    @Schema(description = "ID пользователя", example = "1")
    private Long userId;

    @Schema(description = "Список товаров в заказе")
    private List<OrderItemResponseDto> items;

    @Schema(description = "Общая сумма заказа", example = "199.99")
    private BigDecimal totalAmount;

    @Schema(description = "Статус заказа", example = "NEW")
    private OrderStatus status;

    @Schema(description = "Адрес доставки", example = "Amsterdam, Main Street 10")
    private String shippingAddress;

    @Schema(description = "Способ оплаты", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Дата создания заказа", example = "2026-02-28T10:15:30")
    private LocalDateTime createdAt;
}
