

CREATE TABLE accounts (
                          account_number bigint PRIMARY KEY,
                          routing_number bigint,
                          user_name character varying(255),
                          balance numeric(10,2),
                          created_at timestamp without time zone NOT NULL default CURRENT_TIMESTAMP,
                          updated_at timestamp without time zone NOT NULL default CURRENT_TIMESTAMP
);
-- CREATE UNIQUE INDEX accounts_pkey ON accounts(account_number int8_ops);



CREATE TABLE transactions (
                              id serial PRIMARY KEY,
                              transaction_id bigint,
                              amount numeric(10,2),
                              creditor_account bigint,
                              date timestamp without time zone,
                              debtor_account bigint,
                              memo character varying(255)
);
-- CREATE UNIQUE INDEX transactions_pkey ON transactions(transaction_id int8_ops);