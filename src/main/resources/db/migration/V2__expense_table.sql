CREATE TABLE expense
(
    id         UUID PRIMARY KEY,
    account_id UUID           NOT NULL,
    amount     DECIMAL(19, 2) NOT NULL,
    currency   VARCHAR(3)     NOT NULL DEFAULT 'PLN',
    description VARCHAR(255),
    created_at TIMESTAMP      NOT NULL
);