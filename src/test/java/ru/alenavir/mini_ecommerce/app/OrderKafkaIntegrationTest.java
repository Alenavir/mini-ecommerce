package ru.alenavir.mini_ecommerce.app;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.alenavir.mini_ecommerce.entity.Order;
import ru.alenavir.mini_ecommerce.entity.OrderItem;
import ru.alenavir.mini_ecommerce.entity.Product;
import ru.alenavir.mini_ecommerce.entity.enums.Category;
import ru.alenavir.mini_ecommerce.entity.enums.OrderStatus;
import ru.alenavir.mini_ecommerce.entity.enums.PaymentMethod;
import ru.alenavir.mini_ecommerce.kafka.OrderCreatedEvent;
import ru.alenavir.mini_ecommerce.repo.OrderRepo;
import ru.alenavir.mini_ecommerce.repo.ProductRepo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderKafkaIntegrationTest {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;


    private Product createProduct(String name, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setQuantityInStock(stock);
        p.setPrice(BigDecimal.valueOf(100));
        p.setSku(name + "-SKU");
        p.setCategory(Category.BOOKS);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return productRepo.saveAndFlush(p);
    }

    private Order createOrder(Product product, int quantity) {
        Order order = new Order();
        order.setUserId(1L);
        order.setShippingAddress("Test");
        order.setPaymentMethod(PaymentMethod.CARD);
        order.setStatus(OrderStatus.PROCESSING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());

        order.getItems().add(item);
        order.setTotalAmount(item.getPrice()
                .multiply(BigDecimal.valueOf(quantity)));

        return orderRepo.saveAndFlush(order);
    }

    private void sendEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getItems()
                        .stream()
                        .map(i -> i.getProduct().getId())
                        .toList()
        );

        kafkaTemplate.send("order-events", event);
        kafkaTemplate.flush();
    }

    @Test
    void happyPath_OrderPaid_StockDecreased() {

        Product product = createProduct("happy", 10);
        int initialStock = product.getQuantityInStock();

        Order order = createOrder(product, 1);

        sendEvent(order);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {

            Order updatedOrder =
                    orderRepo.findById(order.getId()).orElseThrow();

            Product updatedProduct =
                    productRepo.findById(product.getId()).orElseThrow();

            assertThat(updatedOrder.getStatus())
                    .isEqualTo(OrderStatus.PAID);

            assertThat(updatedProduct.getQuantityInStock())
                    .isEqualTo(initialStock - 1);
        });
    }

    @Test
    void insufficientStock_OrderCancelled() {

        Product product = createProduct("no-stock", 0);
        Order order = createOrder(product, 1);

        sendEvent(order);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {

            Order updated =
                    orderRepo.findById(order.getId()).orElseThrow();

            Product updatedProduct =
                    productRepo.findById(product.getId()).orElseThrow();

            assertThat(updated.getStatus())
                    .isEqualTo(OrderStatus.CANCELLED);

            assertThat(updatedProduct.getQuantityInStock())
                    .isZero();
        });
    }

    @Test
    void idempotency_EventProcessedOnlyOnce() {

        Product product = createProduct("idempotent", 10);
        int initialStock = product.getQuantityInStock();

        Order order = createOrder(product, 1);

        sendEvent(order);
        sendEvent(order); // duplicate

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {

            Order updated =
                    orderRepo.findById(order.getId()).orElseThrow();

            Product updatedProduct =
                    productRepo.findById(product.getId()).orElseThrow();

            assertThat(updated.getStatus())
                    .isEqualTo(OrderStatus.PAID);

            assertThat(updatedProduct.getQuantityInStock())
                    .isEqualTo(initialStock - 1);
        });
    }


    @Test
    void messageGoesToDLT_WhenConsumerFails() {

        // событие с несуществующим orderId
        OrderCreatedEvent invalid =
                new OrderCreatedEvent(999999L, 1L, List.of(1L));

        kafkaTemplate.send("order-events", invalid);
        kafkaTemplate.flush();

        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("group.id", "dlt-test");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.springframework.kafka.support.serializer.JsonDeserializer");
        props.put("spring.json.trusted.packages", "*");
        props.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, OrderCreatedEvent> consumer =
                     new KafkaConsumer<>(props)) {

            consumer.subscribe(List.of("order-events.DLT"));

            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {

                var records = consumer.poll(Duration.ofMillis(500));

                boolean found = false;
                for (ConsumerRecord<String, OrderCreatedEvent> r :
                        records.records("order-events.DLT")) {
                    if (r.value().getOrderId().equals(999999L)) {
                        found = true;
                        break;
                    }
                }

                assertThat(found).isTrue();
            });
        }
    }

    @Test
    void parallelOrders_StockHandledCorrectly() throws InterruptedException {

        Product product = createProduct("parallel", 5);

        int totalOrders = 10;
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < totalOrders; i++) {
            orders.add(createOrder(product, 1));
        }

        ExecutorService executor = Executors.newFixedThreadPool(totalOrders);

        for (Order order : orders) {
            executor.submit(() -> sendEvent(order));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {

            Product updatedProduct = productRepo.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getQuantityInStock()).isZero();

            long paidCount = orders.stream()
                    .map(o -> orderRepo.findById(o.getId()).orElseThrow())
                    .filter(o -> o.getStatus() == OrderStatus.PAID)
                    .count();

            long cancelledCount = orders.stream()
                    .map(o -> orderRepo.findById(o.getId()).orElseThrow())
                    .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                    .count();

            assertThat(paidCount).isEqualTo(5);
            assertThat(cancelledCount).isEqualTo(totalOrders - 5);
        });
    }
}