CREATE TABLE IF NOT EXISTS daily_report (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    total_in_base_currency DECIMAL(15, 2) NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE
);