CREATE TABLE transaction
(
    id               UUID PRIMARY KEY,
    account_id       UUID                     NOT NULL,
    amount           DECIMAL(19, 2)           NOT NULL,
    currency         VARCHAR(3)               NOT NULL,
    type             VARCHAR(50)              NOT NULL,
    description      VARCHAR(200)             NOT NULL,
    category         VARCHAR(100),
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT check_type CHECK (type IN ('INCOME', 'EXPENSE'))
);