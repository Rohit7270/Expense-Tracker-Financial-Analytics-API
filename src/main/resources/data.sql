-- Seed Roles
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

-- Seed Default Categories safely
INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Food & Dining', 'EXPENSE', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Food & Dining' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Utilities', 'EXPENSE', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Utilities' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Rent & Housing', 'EXPENSE', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Rent & Housing' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Transportation', 'EXPENSE', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Transportation' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Entertainment', 'EXPENSE', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Entertainment' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Healthcare & Medical', 'EXPENSE', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Healthcare & Medical' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Salary', 'INCOME', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Salary' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Freelance', 'INCOME', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Freelance' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Investments', 'INCOME', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Investments' AND user_id IS NULL);

INSERT INTO categories (name, type, created_at, user_id)
SELECT 'Miscellaneous', 'BOTH', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Miscellaneous' AND user_id IS NULL);

-- Seed Default Payment Methods safely
INSERT INTO payment_methods (name, created_at, user_id)
SELECT 'Cash', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM payment_methods WHERE name = 'Cash' AND user_id IS NULL);

INSERT INTO payment_methods (name, created_at, user_id)
SELECT 'Credit Card', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM payment_methods WHERE name = 'Credit Card' AND user_id IS NULL);

INSERT INTO payment_methods (name, created_at, user_id)
SELECT 'Debit Card', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM payment_methods WHERE name = 'Debit Card' AND user_id IS NULL);

INSERT INTO payment_methods (name, created_at, user_id)
SELECT 'Bank Transfer', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM payment_methods WHERE name = 'Bank Transfer' AND user_id IS NULL);

INSERT INTO payment_methods (name, created_at, user_id)
SELECT 'UPI / Mobile Wallet', CURRENT_TIMESTAMP, NULL WHERE NOT EXISTS (SELECT 1 FROM payment_methods WHERE name = 'UPI / Mobile Wallet' AND user_id IS NULL);
