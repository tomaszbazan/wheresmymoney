CREATE TABLE audit_log
(
    id                 UUID PRIMARY KEY,
    operation          VARCHAR(20)              NOT NULL CHECK (operation IN ('CREATE', 'UPDATE', 'DELETE')),
    entity_type        VARCHAR(50)              NOT NULL CHECK (entity_type IN ('ACCOUNT', 'TRANSACTION', 'CATEGORY')),
    entity_id          UUID                     NOT NULL,
    performed_by       VARCHAR(100)             NOT NULL,
    group_id           UUID                     NOT NULL,
    performed_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_description VARCHAR(500)
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_group ON audit_log (group_id, performed_at DESC);
CREATE INDEX idx_audit_log_user ON audit_log (performed_by, performed_at DESC);
CREATE INDEX idx_audit_log_timestamp ON audit_log (performed_at DESC);
