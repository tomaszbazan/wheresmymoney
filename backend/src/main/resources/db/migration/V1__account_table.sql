CREATE TABLE account
(
    id       UUID PRIMARY KEY,
    name     VARCHAR(255)   NOT NULL,
    balance  DECIMAL(19, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3)     NOT NULL DEFAULT 'PLN'
);