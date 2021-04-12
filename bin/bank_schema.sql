

CREATE TABLE accounts (
                          account_number bigint PRIMARY KEY,
                          balance numeric(19,2),
                          routing_number bigint,
                          user_name character varying(255)
);




CREATE TABLE transactions (
                              transaction_id bigint PRIMARY KEY,
                              amount numeric(19,2),
                              creditor_account bigint,
                              date timestamp without time zone,
                              debtor_account bigint,
                              memo character varying(255)
);