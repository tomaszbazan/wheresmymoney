ALTER TABLE transaction
    ADD COLUMN transaction_date DATE        NOT NULL DEFAULT CURRENT_DATE,
    ADD COLUMN transaction_hash VARCHAR(64) NOT NULL DEFAULT '';

CREATE INDEX idx_transaction_account_hash
    ON transaction (account_id, transaction_hash);

ALTER TABLE transaction
    ALTER COLUMN transaction_date DROP DEFAULT,
    ALTER COLUMN transaction_hash DROP DEFAULT;
