CREATE TABLE IF NOT EXISTS cash_balance (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    currency_id UUID NOT NULL REFERENCES currency(id),
    amount DECIMAL(15, 2) NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE
);