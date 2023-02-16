CREATE TABLE users
(
    id  serial PRIMARY KEY ,
    username TEXT,
    first_name TEXT,
    last_name TEXT,
    phone TEXT,
    reg_date TIMESTAMP,
    city TEXT,
    image BYTEA,
    password TEXT,
    role text,
    enabled bool
);