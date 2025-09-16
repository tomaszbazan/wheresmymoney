CREATE TABLE account
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(255)             NOT NULL,
    balance           DECIMAL(19, 2)           NOT NULL DEFAULT 0,
    currency          VARCHAR(3)               NOT NULL DEFAULT 'PLN',
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(100)             NOT NULL,
    created_by_group  UUID                     NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(100)             NOT NULL,
    data              JSONB                             DEFAULT '{}'::jsonb
);