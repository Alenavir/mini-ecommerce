CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

COMMENT ON TABLE users IS 'Таблица пользователей системы';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.name IS 'Имя пользователя';
COMMENT ON COLUMN users.email IS 'Email пользователя';
COMMENT ON COLUMN users.password_hash IS 'Хеш пароля пользователя';
COMMENT ON COLUMN users.is_active IS 'Флаг активности аккаунта';
COMMENT ON COLUMN users.created_at IS 'Дата и время создания пользователя';
COMMENT ON COLUMN users.updated_at IS 'Дата и время последнего обновления пользователя';

CREATE TABLE IF NOT EXISTS user_roles (
    user_id INTEGER NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    );

COMMENT ON TABLE user_roles IS 'Таблица ролей пользователей (many-to-many через enum)';
COMMENT ON COLUMN user_roles.user_id IS 'Идентификатор пользователя';
COMMENT ON COLUMN user_roles.role IS 'Роль пользователя (USER, ADMIN)';

CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);
COMMENT ON INDEX idx_users_name IS 'Индекс для ускоренного поиска пользователей по имени';