CREATE TABLE transfer
(
    id                UUID PRIMARY KEY,
    source_account_id UUID                     NOT NULL,
    target_account_id UUID                     NOT NULL,
    source_amount     DECIMAL(19, 2)           NOT NULL,
    source_currency   VARCHAR(3)               NOT NULL,
    target_amount     DECIMAL(19, 2)           NOT NULL,
    target_currency   VARCHAR(3)               NOT NULL,
    exchange_rate     DECIMAL(19, 6)           NOT NULL,
    description       VARCHAR(200),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by        VARCHAR(100)             NOT NULL,
    created_by_group  UUID                     NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by        VARCHAR(100)             NOT NULL,
    is_deleted        BOOLEAN                           DEFAULT false,
    deleted_at        TIMESTAMP WITH TIME ZONE          DEFAULT NULL,
    version           BIGINT                   NOT NULL DEFAULT 0,
    CONSTRAINT check_different_accounts CHECK (source_account_id != target_account_id
) ,
    CONSTRAINT check_positive_amounts CHECK (source_amount > 0 AND target_amount > 0),
    CONSTRAINT check_positive_rate CHECK (exchange_rate > 0)
);

CREATE INDEX idx_transfer_source_account ON transfer (source_account_id);
CREATE INDEX idx_transfer_target_account ON transfer (target_account_id);
CREATE INDEX idx_transfer_group ON transfer (created_by_group);
