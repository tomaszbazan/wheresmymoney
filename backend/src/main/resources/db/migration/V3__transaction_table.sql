CREATE TABLE transaction
(
    id               UUID PRIMARY KEY,
    account_id       UUID                     NOT NULL,
    amount           DECIMAL(19, 2)           NOT NULL,
    currency         VARCHAR(3)               NOT NULL,
    type             VARCHAR(50)              NOT NULL,
    description      VARCHAR(200),
    category_id      UUID                     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by       VARCHAR(100)             NOT NULL,
    created_by_group UUID                     NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by       VARCHAR(100)             NOT NULL,
    updated_by_group UUID                     NOT NULL,
    is_deleted       BOOLEAN                  DEFAULT false,
    deleted_at       TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    CONSTRAINT check_type CHECK (type IN ('INCOME', 'EXPENSE'))
);