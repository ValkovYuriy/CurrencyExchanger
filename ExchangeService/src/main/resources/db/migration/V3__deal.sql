CREATE TABLE IF NOT EXISTS deal(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id),
    from_currency_code VARCHAR(10) NOT NULL,
    to_currency_code VARCHAR(10) NOT NULL,
    amount_from DECIMAL(15, 2) NOT NULL,
    amount_to DECIMAL(15, 2) NOT NULL,
    exchange_rate DECIMAL(10, 4) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
)