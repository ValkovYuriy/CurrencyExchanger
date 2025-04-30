CREATE OR REPLACE FUNCTION enforce_uppercase_code()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.code := upper(NEW.code);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER currency_code_uppercase
BEFORE INSERT OR UPDATE ON currency
FOR EACH ROW EXECUTE FUNCTION enforce_uppercase_code();