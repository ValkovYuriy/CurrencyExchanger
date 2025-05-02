CREATE TABLE IF NOT EXISTS "dealUser"(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(30) UNIQUE NOT NULL ,
    password CHAR(60) NOT NULL
);
CREATE TABLE IF NOT EXISTS role(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    role VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS deal(
   id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "dealUser"(id),
    from_currency_id UUID NOT NULL REFERENCES currency(id),
    to_currency_id UUID NOT NULL REFERENCES currency(id),
    amount_from DECIMAL(15, 2) NOT NULL,
    amount_to DECIMAL(15, 2) NOT NULL,
    exchange_rate DECIMAL(10, 4) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS user_roles(
    user_id UUID REFERENCES "dealUser"(id),
    role_id UUID REFERENCES role(id),
    PRIMARY KEY (user_id,role_id)
);
CREATE TABLE IF NOT EXISTS currency (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS exchange_rate (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    base_currency_id UUID NOT NULL REFERENCES currency(id),
    target_currency_id UUID NOT NULL REFERENCES currency(id),
    rate DECIMAL(10, 4) NOT NULL,
    date DATE NOT NULL
);
CREATE TABLE IF NOT EXISTS cash_balance (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    currency_id UUID NOT NULL REFERENCES currency(id),
    amount DECIMAL(15, 2) NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE
);
CREATE TABLE IF NOT EXISTS daily_report (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    total_in_base_currency DECIMAL(15, 2) NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE
);
