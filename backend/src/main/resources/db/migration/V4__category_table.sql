CREATE TABLE category
(
    id               UUID PRIMARY KEY,
    name             VARCHAR(100)             NOT NULL,
    description      TEXT,
    type             VARCHAR(50)              NOT NULL,
    color            VARCHAR(7)               NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by       VARCHAR(100)             NOT NULL,
    created_by_group UUID                     NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by       VARCHAR(100)             NOT NULL,
    updated_by_group UUID                     NOT NULL,
    is_deleted       BOOLEAN                  DEFAULT false,
    deleted_at       TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    CONSTRAINT check_category_type CHECK (type IN ('INCOME', 'EXPENSE'))
);