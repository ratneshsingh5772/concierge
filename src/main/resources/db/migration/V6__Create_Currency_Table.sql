CREATE TABLE currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    symbol VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(255)
);

INSERT INTO currencies (code, symbol, name, country) VALUES
('USD', '$', 'US Dollar', 'United States'),
('EUR', '€', 'Euro', 'European Union'),
('GBP', '£', 'British Pound', 'United Kingdom'),
('JPY', '¥', 'Japanese Yen', 'Japan'),
('INR', '₹', 'Indian Rupee', 'India'),
('AUD', 'A$', 'Australian Dollar', 'Australia'),
('CAD', 'C$', 'Canadian Dollar', 'Canada'),
('CHF', 'CHF', 'Swiss Franc', 'Switzerland'),
('CNY', '¥', 'Chinese Yuan', 'China');

