CREATE TABLE transaction
(
    id          UUID PRIMARY KEY,
    account_id  UUID                     NOT NULL,
    amount      DECIMAL(19, 2)           NOT NULL,
    currency    VARCHAR(3)               NOT NULL,
    type        VARCHAR(50)              NOT NULL,
    description VARCHAR(200),
    category    VARCHAR(100)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    is_deleted  BOOLEAN                  DEFAULT false,
    deleted_at  TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    CONSTRAINT check_type CHECK (type IN ('INCOME', 'EXPENSE'))
);