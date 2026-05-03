package ru.alenavir.mini_ecommerce;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.alenavir.mini_ecommerce.BaseIntegrationTest;
import ru.alenavir.mini_ecommerce.dto.order.OrderCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderItemCreateDto;
import ru.alenavir.mini_ecommerce.dto.order.OrderResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.JwtAuthenticationDto;
import ru.alenavir.mini_ecommerce.dto.user.auth.UserCredentialsDto;
import ru.alenavir.mini_ecommerce.entity.OutboxEvent;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.User;
import ru.alenavir.mini_ecommerce.entity.enums.Category;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.entity.enums.PaymentMethod;
import ru.alenavir.mini_ecommerce.entity.enums.Role;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.OutboxEventRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;
import ru.alenavir.mini_ecommerce.repo.UserRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OutboxEventRepo outboxEventRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        outboxEventRepo.deleteAll();
        orderRepo.deleteAll();
        productRepo.deleteAll();
        userRepo.deleteAll();

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@mail.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRoles(Set.of(Role.USER));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userId = userRepo.save(user).getId();

        Product product = new Product();
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setPrice(BigDecimal.valueOf(100));
        product.setQuantityInStock(1000);
        product.setCategory(Category.BOOKS);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productId = productRepo.save(product).getId();

        UserCredentialsDto credentials = new UserCredentialsDto();
        credentials.setEmail("test@mail.com");
        credentials.setPassword("password123");

        ResponseEntity<JwtAuthenticationDto> authResponse = restTemplate.postForEntity(
                "/api/v1/auth/sign-in",
                credentials,
                JwtAuthenticationDto.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        token = authResponse.getBody().getToken();
    }

    private <T> HttpEntity<T> withAuth(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void createOrder_shouldSaveOrderAndOutboxEvent() {
        OrderCreateDto dto = buildOrderCreateDto(2);

        ResponseEntity<OrderResponseDto> response = restTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                withAuth(dto),
                OrderResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();

        Long orderId = response.getBody().getId();

        Order saved = orderRepo.findById(orderId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));

        List<OutboxEvent> outboxEvents = outboxEventRepo.findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.PENDING);
        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.get(0).getEventType()).isEqualTo("OrderCreatedEvent");
    }

    @Test
    void createOrder_shouldBecomePaidAfterKafkaProcessing() {
        OrderCreateDto dto = buildOrderCreateDto(2);

        ResponseEntity<OrderResponseDto> response = restTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                withAuth(dto),
                OrderResponseDto.class
        );

        Long orderId = response.getBody().getId();

        await()
                .atMost(30, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    Order order = orderRepo.findById(orderId).orElseThrow();
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
                });

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    List<OutboxEvent> sent = outboxEventRepo.findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.SENT);
                    assertThat(sent).hasSize(1);
                });
    }

    @Test
    void createOrder_shouldBeCancelledWhenInsufficientStock() {
        OrderCreateDto dto = buildOrderCreateDto(9999);

        ResponseEntity<OrderResponseDto> response = restTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                withAuth(dto),
                OrderResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long orderId = response.getBody().getId();

        await()
                .atMost(30, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    Order order = orderRepo.findById(orderId).orElseThrow();
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                });
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        OrderCreateDto dto = buildOrderCreateDto(1);
        ResponseEntity<OrderResponseDto> created = restTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                withAuth(dto),
                OrderResponseDto.class
        );
        Long orderId = created.getBody().getId();

        ResponseEntity<OrderResponseDto> response = restTemplate.exchange(
                "/api/v1/orders/" + orderId,
                HttpMethod.GET,
                withAuth(null),
                OrderResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(orderId);
        assertThat(response.getBody().getUserId()).isEqualTo(userId);
    }

    @Test
    void createOrder_shouldReturn404_whenProductNotFound() {
        OrderItemCreateDto item = new OrderItemCreateDto();
        item.setProductId(99999L);
        item.setQuantity(1);

        OrderCreateDto dto = new OrderCreateDto();
        dto.setUserId(userId);
        dto.setItems(List.of(item));
        dto.setShippingAddress("ул. Пушкина д.1");
        dto.setPaymentMethod(PaymentMethod.CARD);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                withAuth(dto),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createOrder_shouldReturn401_whenNoToken() {
        OrderCreateDto dto = buildOrderCreateDto(1);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/orders",
                dto,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private OrderCreateDto buildOrderCreateDto(int quantity) {
        OrderItemCreateDto item = new OrderItemCreateDto();
        item.setProductId(productId);
        item.setQuantity(quantity);

        OrderCreateDto dto = new OrderCreateDto();
        dto.setUserId(userId);
        dto.setItems(List.of(item));
        dto.setShippingAddress("ул. Пушкина, д. 10, Москва");
        dto.setPaymentMethod(PaymentMethod.CARD);
        return dto;
    }
}
