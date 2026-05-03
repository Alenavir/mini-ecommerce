CREATE TABLE IF NOT EXISTS outbox_events (
    id              UUID            PRIMARY KEY,
    event_type      VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP       NOT NULL,
    attempts        INT             NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP
    );

-- индекс для быстрого поиска PENDING событий
CREATE INDEX idx_outbox_events_status ON outbox_events(status);