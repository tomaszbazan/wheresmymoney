ALTER TABLE transaction ADD COLUMN bill JSONB;

ALTER TABLE transaction DROP COLUMN amount;
ALTER TABLE transaction DROP COLUMN currency;
ALTER TABLE transaction DROP COLUMN description;
ALTER TABLE transaction DROP COLUMN category_id;
