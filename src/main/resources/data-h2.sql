-- Seed Roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Seed Default Categories
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Food & Dining', 'EXPENSE', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Utilities', 'EXPENSE', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Rent & Housing', 'EXPENSE', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Transportation', 'EXPENSE', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Entertainment', 'EXPENSE', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Healthcare & Medical', 'EXPENSE', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Salary', 'INCOME', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Freelance', 'INCOME', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Investments', 'INCOME', CURRENT_TIMESTAMP, NULL);
INSERT INTO categories (name, type, created_at, user_id) VALUES ('Miscellaneous', 'BOTH', CURRENT_TIMESTAMP, NULL);

-- Seed Default Payment Methods
INSERT INTO payment_methods (name, created_at, user_id) VALUES ('Cash', CURRENT_TIMESTAMP, NULL);
INSERT INTO payment_methods (name, created_at, user_id) VALUES ('Credit Card', CURRENT_TIMESTAMP, NULL);
INSERT INTO payment_methods (name, created_at, user_id) VALUES ('Debit Card', CURRENT_TIMESTAMP, NULL);
INSERT INTO payment_methods (name, created_at, user_id) VALUES ('Bank Transfer', CURRENT_TIMESTAMP, NULL);
INSERT INTO payment_methods (name, created_at, user_id) VALUES ('UPI / Mobile Wallet', CURRENT_TIMESTAMP, NULL);
