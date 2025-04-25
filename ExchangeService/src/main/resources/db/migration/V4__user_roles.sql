CREATE TABLE IF NOT EXISTS user_roles(
    user_id UUID REFERENCES "user"(id),
    role_id UUID REFERENCES role(id),
    PRIMARY KEY (user_id,role_id)
)