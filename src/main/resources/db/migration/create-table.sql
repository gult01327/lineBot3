CREATE TABLE detail
(
    shop_name text COLLATE pg_catalog."default",
    user_name text COLLATE pg_catalog."default",
    drink text COLLATE pg_catalog."default",
    sugar text COLLATE pg_catalog."default",
    ice text COLLATE pg_catalog."default",
    size text COLLATE pg_catalog."default",
    price integer,
    inputdate date,
    update date,
    update_name text COLLATE pg_catalog."default",
    status text COLLATE pg_catalog."default"
);

CREATE TABLE main
(
    shop_name text COLLATE pg_catalog."default",
    date text COLLATE pg_catalog."default",
    sum_price integer,
    inputdate date,
    update date,
    update_name text COLLATE pg_catalog."default",
    status text COLLATE pg_catalog."default"
)