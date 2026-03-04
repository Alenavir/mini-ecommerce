# ![Mini E-Commerce Backend](https://img.shields.io/badge/Mini%20E--Commerce-Backend-blue)

![Java](https://img.shields.io/badge/Java-17-orange)
![Kafka](https://img.shields.io/badge/Kafka-3.5-yellow)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Liquibase](https://img.shields.io/badge/Liquibase-latest-blueviolet)
![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-orange)
![Lombok](https://img.shields.io/badge/Lombok-1.18.28-lightgrey)
![Spring Retry](https://img.shields.io/badge/Spring_Retry-2.0.12-green)
![Springdoc OpenAPI](https://img.shields.io/badge/Springdoc-2.5.0-blue)
![Docker](https://img.shields.io/badge/Docker-24.0-blue)
---

## 🚀 Описание проекта
**Backend проект** для мини e-commerce системы:

- CRUD для **Users**, **Products** и **Orders**
- JWT-аутентификация с ролями `USER` / `ADMIN`
- Асинхронная обработка заказов через **Kafka / RabbitMQ**
- **Idempotency**, **Retry** и **Dead Letter Queue**
- Транзакции и **Optimistic Locking**
- Кеширование последних заказов через **Redis**
- Контейнеризация через Docker и docker-compose
- Swagger / Postman документация API

---

## 🏗 Стек технологий

| Слой | Технологии |
|------|--------|
| Язык | Java 17 |
| Фреймворк | Spring Boot |
| База данных | PostgreSQL |
| Сообщения | Kafka |
| Кеширование | Redis |
| Безопасность | Spring Security + JWT |
| Документация | Swagger |
| Контейнеризация | Docker, docker-compose |
| Тестирование | JUnit |

---

## ⚙️ Функциональность

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

---
