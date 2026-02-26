CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    category VARCHAR(50),
    sku VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE products IS 'Таблица продуктов системы';
COMMENT ON COLUMN products.id IS 'Уникальный идентификатор товара';
COMMENT ON COLUMN products.name IS 'Название товара';
COMMENT ON COLUMN products.description IS 'Описание товара';
COMMENT ON COLUMN products.price IS 'Цена товара';
COMMENT ON COLUMN products.quantity_in_stock IS 'Кол-во ед. на складе';
COMMENT ON COLUMN products.category IS 'Категория товара';
COMMENT ON COLUMN products.sku IS 'Артикль товара';
COMMENT ON COLUMN products.created_at IS 'Дата и время создания товара';
COMMENT ON COLUMN products.updated_at IS 'Дата и время последнего обновления товара';
COMMENT ON COLUMN products.version IS 'Версия записи для Optimistic Locking';