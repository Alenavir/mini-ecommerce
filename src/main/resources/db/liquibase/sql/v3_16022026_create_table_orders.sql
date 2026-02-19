CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    total_amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
     id SERIAL PRIMARY KEY,
     order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
     product_id INTEGER NOT NULL REFERENCES products(id),
     quantity INT NOT NULL DEFAULT 1
);

COMMENT ON TABLE orders IS 'Таблица заказов пользователей';
COMMENT ON COLUMN orders.id IS 'Уникальный идентификатор заказа';
COMMENT ON COLUMN orders.user_id IS 'Идентификатор пользователя, оформившего заказ';
COMMENT ON COLUMN orders.total_amount IS 'Общая сумма заказа';
COMMENT ON COLUMN orders.status IS 'Статус заказа (NEW, PAID, SHIPPED, CANCELLED и т.д.)';
COMMENT ON COLUMN orders.shipping_address IS 'Адрес доставки';
COMMENT ON COLUMN orders.payment_method IS 'Метод оплаты (CASH, CARD)';
COMMENT ON COLUMN orders.created_at IS 'Дата и время создания заказа';
COMMENT ON COLUMN orders.updated_at IS 'Дата и время последнего обновления заказа';


COMMENT ON TABLE order_items IS 'Товары, входящие в каждый заказ';
COMMENT ON COLUMN order_items.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN order_items.order_id IS 'Идентификатор заказа, к которому относится товар';
COMMENT ON COLUMN order_items.product_id IS 'Идентификатор товара';
COMMENT ON COLUMN order_items.quantity IS 'Количество единиц товара в заказе';

