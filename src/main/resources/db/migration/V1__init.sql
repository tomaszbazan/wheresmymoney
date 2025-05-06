CREATE TABLE account (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE expense (
    id UUID PRIMARY KEY
--     account_id UUID NOT NULL,
--     amount DECIMAL(19, 2) NOT NULL,
--     description VARCHAR(255),
--     date TIMESTAMP NOT NULL,
--     FOREIGN KEY (account_id) REFERENCES accounts(id)
);
--
-- -- Create account_expenses table for the many-to-many relationship
-- CREATE TABLE account_expenses (
--     account_id UUID NOT NULL,
--     expense_id UUID NOT NULL,
--     PRIMARY KEY (account_id, expense_id),
--     FOREIGN KEY (account_id) REFERENCES accounts(id),
--     FOREIGN KEY (expense_id) REFERENCES expenses(id)
-- );