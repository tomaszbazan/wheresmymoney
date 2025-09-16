ALTER TABLE category
    ADD COLUMN parent_id UUID,
    ADD CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id)
            REFERENCES category (id);