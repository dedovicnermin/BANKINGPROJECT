

CREATE TABLE accounts (
                          account_number bigint PRIMARY KEY,
                          balance numeric(19,2),
                          routing_number bigint,
                          user_name character varying(255)
);
-- CREATE UNIQUE INDEX accounts_pkey ON accounts(account_number int8_ops);



CREATE TABLE transactions (
                              transaction_id bigint PRIMARY KEY,
                              amount numeric(19,2),
                              creditor_account bigint,
                              date timestamp without time zone,
                              debtor_account bigint,
                              memo character varying(255)
);
-- CREATE UNIQUE INDEX transactions_pkey ON transactions(transaction_id int8_ops);