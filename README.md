# Mini E-Commerce Backend

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka_3.7-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/Redis_7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?style=for-the-badge&logo=liquibase&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct_1.6.3-ED8B00?style=for-the-badge&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![k6](https://img.shields.io/badge/k6_load_tested-7D64FF?style=for-the-badge&logo=k6&logoColor=white)

Backend для мини e-commerce системы. Реализует управление пользователями, продуктами и заказами с асинхронной обработкой через Kafka, кешированием в Redis и надёжной доставкой событий через Outbox pattern.

---

## Стек технологий

| Слой | Технологии |
|---|---|
| Язык | Java 17 |
| Фреймворк | Spring Boot 3.3.5 |
| База данных | PostgreSQL 15 + Liquibase |
| Очередь сообщений | Apache Kafka 3.7 (KRaft mode) |
| Кеширование | Redis 7 |
| Безопасность | Spring Security + JWT (HS512) |
| Маппинг | MapStruct 1.6.3 |
| Документация | Springdoc OpenAPI (Swagger UI) |
| Метрики | Micrometer + Actuator |
| Контейнеризация | Docker + Docker Compose |
| Тестирование | JUnit 5 + Mockito |

---

### ⚙️ Функциональность

### Orders
- **Создание заказа** с проверкой наличия товаров
- **Получение заказа по ID** с кешированием
- **Получение последних заказов пользователя** (с ограничением на 10)
- **Получение всех заказов**
- **Обновление заказа** (с сохранением истории и кеша)
- **Удаление заказа** с кеш-evict
- **Смена статуса заказа** с проверкой возможности перехода (например, `PROCESSING` → `PAID`)
- Метрики через `MeterRegistry` для всех операций

### Products
- **Создание продукта**
- **Поиск продуктов** по фильтру (name, SKU, категория, диапазон цен)
- **Получение продукта по ID** с кешированием
- **Обновление продукта** с кеш-поддержкой
- **Удаление продукта** с кеш-evict
- Метрики через `MeterRegistry` для всех операций

### Users
- **Создание пользователя**
- **Поиск пользователей** по email и имени
- **Получение пользователя по ID** с кешированием
- **Обновление пользователя** (обычное и админское)
- **Деактивация пользователя**
- **Удаление пользователя** с кеш-evict
- **Аутентификация (signIn)** с генерацией JWT
- **Обновление токена (refreshToken)** через refresh-token
- Метрики через `MeterRegistry` для всех операций, включая успешные и неуспешные попытки входа


## Архитектура

```
HTTP Request
     │
     ▼
Controller → Service → Repository → PostgreSQL
                │
                ▼
          OutboxEvent (та же транзакция)
                │
                ▼ @Scheduled каждые 5 сек
        OutboxEventPublisher
                │
                ▼
        Kafka Topic: order-events
                │
                ├── успех → статус SENT
                └── ошибка → retry (ExponentialBackOff)
                              └── после 5 попыток → FAILED + ERROR log
                                                  → order-events.DLT
                │
                ▼
        OrderEventConsumer
                │
                ├── Idempotency check (таблица processed_events)
                ├── проверка наличия товаров
                ├── списание stock (batch saveAll)
                └── заказ → PAID / CANCELLED
```

### Почему Kafka в одном сервисе

Kafka выбрана намеренно для демонстрации паттернов надёжной асинхронной обработки:
- HTTP-ответ не блокируется на время обработки заказа
- Retry + DLQ из коробки
- Задел под будущее разделение на микросервисы

---

## Ключевые технические решения

### Transactional Outbox Pattern

Заказ и событие сохраняются в одной транзакции БД. Отдельный планировщик читает `outbox_events` и доставляет в Kafka. Это гарантирует что заказ никогда не потеряется даже если Kafka недоступна в момент создания.

```
BEGIN TRANSACTION
  INSERT INTO orders ...
  INSERT INTO outbox_events (status = PENDING) ...
COMMIT
```

### Idempotency

Consumer проверяет таблицу `processed_events` перед обработкой. При повторной доставке того же события — пропускает обработку. Защищает от дублирования при at-least-once семантике Kafka.

### Optimistic Locking

Используется для предотвращения race condition при параллельном обновлении заказов и продуктов.

### Кеширование (Redis)

| Кеш | TTL | Описание |
|---|---|---|
| `orders` | 10 мин | Заказ по ID |
| `lastOrders` | 5 мин | Последние 10 заказов пользователя |
| `products` | 30 мин | Продукт по ID |
| `users` | 15 мин | Пользователь по ID |

При изменении данных кеш инвалидируется через `@CacheEvict`.

### Dead Letter Queue

При неудачной обработке события Consumer делает retry с экспоненциальной задержкой (1s → 2s → 4s → ... → 10s). После исчерпания попыток сообщение уходит в топик `order-events.DLT`.

---

## API

Полная документация доступна через Swagger UI после запуска:

```
http://localhost:8080/swagger-ui/index.html
```

### Жизненный цикл заказа

```
PROCESSING → PAID → SHIPPED → DELIVERED
           ↘
           CANCELLED
```

---

## Запуск

### Требования

- Docker + Docker Compose

### Шаги

1. Клонировать репозиторий:
```bash
git clone https://github.com/alenavir/mini-ecommerce.git
cd mini-ecommerce
```

2. Создать `.env` файл на основе примера:
```bash
cp .env.example .env
```

3. Запустить все сервисы:
```bash
docker compose up --build
```

4. Открыть Swagger UI: http://localhost:8080/swagger-ui/index.html

### Переменные окружения

| Переменная | Описание |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Пользователь БД |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД |
| `JWT_SECRET` | Секрет для подписи JWT (Base64, min 512 bit) |
| `REDIS_HOST` | Хост Redis |
| `REDIS_PORT` | Порт Redis |
| `KAFKA_BOOTSTRAP_SERVERS` | Адрес Kafka брокера |

---

## Метрики

Доступны через Spring Actuator:

```
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/health
```

| Метрика | Описание |
|---|---|
| `orders.created` | Количество созданных заказов |
| `orders_processed_total` | Успешно обработанных через Kafka |
| `orders_cancelled_total` | Отменённых из-за нехватки товара |
| `order_processing_duration_seconds` | Время обработки заказа |
| `outbox.events.sent` | Событий доставлено в Kafka |
| `outbox.events.failed` | Событий не удалось доставить после 5 попыток |
| `kafka.order.events.sent` | Подтверждений от Kafka брокера |

---

## Нагрузочное тестирование

Проводилось через [k6](https://k6.io/) локально, все сервисы запущены через Docker Compose на одной машине.

Сценарий: 150 виртуальных пользователей одновременно отправляют `POST /api/v1/orders` в течение 30 секунд с паузой 1 секунда между итерациями.

### Результаты

| Параметр | Значение |
|---|---|
| Виртуальных пользователей | 150 |
| Длительность | 30 секунд |
| Всего запросов | 4463 |
| Успешных | **100% (0 ошибок)** |
| Медиана | **5ms** |
| p95 | 26ms |
| Максимум | 915ms |
| Пропускная способность | ~144 req/s |

Для запуска скопируй свой JWT токен в переменную `TOKEN` в файле `load-test.js`, затем:
```bash
k6 run load-test.js
```
```bash
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 150,        // виртуальных пользователей
    duration: '30s', // длительность теста
};

const BASE_URL = 'http://localhost:8080';

// Получить токен через POST /api/v1/auth/sign-in и вставить сюда
const TOKEN = 'YOUR_JWT_TOKEN';

export default function () {
    const payload = JSON.stringify({
        userId: 1,
        items: [
            {
                productId: 1,
                quantity: 5
            }
        ],
        shippingAddress: "ул. Пушкина, д. 10, кв. 25, Москва, 101000",
        paymentMethod: "CARD"
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${TOKEN}`,
        },
    };

    const res = http.post(`${BASE_URL}/api/v1/orders`, payload, params);

    check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    });

    sleep(1);
}

```
---

## CI/CD

Настроен GitHub Actions пайплайн с двумя джобами (.github/workflows/ci.yml).

**Build & Unit Tests** — запускается на каждый push и pull request в `master`:
- Собирает приложение через Maven
- Запускает unit тесты
- Сохраняет JAR как артефакт для следующего шага
  **Deploy Docker Image** — запускается после успешного билда:
- Скачивает JAR из артефактов
- Логинится в DockerHub через секреты репозитория
- Собирает Docker образ и пушит как `latest`

Скачать образ можно с DockerHub:
```
docker push alenavir/mini-ecommerce:tagname
```