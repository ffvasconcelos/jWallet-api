--liquibase formatted sql

--changeset jwallet:002-business-checks dbms:postgresql
-- The Hibernate diff generator does not emit these entity CheckConstraints.
ALTER TABLE wallets ADD CONSTRAINT ck_wallets_balance_non_negative CHECK (balance >= 0);
ALTER TABLE transfers ADD CONSTRAINT ck_transfers_amount_positive CHECK (amount > 0);
ALTER TABLE transfers ADD CONSTRAINT ck_transfers_different_wallets CHECK (source_wallet_id <> target_wallet_id);
ALTER TABLE transfers ADD CONSTRAINT ck_transfers_status CHECK (status IN ('COMPLETED'));

--rollback ALTER TABLE transfers DROP CONSTRAINT ck_transfers_status;
--rollback ALTER TABLE transfers DROP CONSTRAINT ck_transfers_different_wallets;
--rollback ALTER TABLE transfers DROP CONSTRAINT ck_transfers_amount_positive;
--rollback ALTER TABLE wallets DROP CONSTRAINT ck_wallets_balance_non_negative;
