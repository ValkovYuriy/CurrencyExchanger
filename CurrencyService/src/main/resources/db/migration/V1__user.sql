CREATE TABLE IF NOT EXISTS "user"(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(30) UNIQUE NOT NULL ,
    password CHAR(60) NOT NULL
)