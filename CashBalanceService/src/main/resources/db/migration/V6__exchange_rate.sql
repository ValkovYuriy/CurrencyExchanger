CREATE TABLE IF NOT EXISTS exchange_rate (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    base_currency_id UUID NOT NULL REFERENCES currency(id),
    target_currency_id UUID NOT NULL REFERENCES currency(id),
    rate DECIMAL(10, 4) NOT NULL,
    date DATE NOT NULL,
    UNIQUE (base_currency_id,target_currency_id,date)
);