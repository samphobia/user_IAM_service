create table if not exists bank_accounts (
    id uuid primary key,
    user_id varchar(100) not null,
    mono_account_id varchar(120) unique,
    institution_code varchar(64),
    account_number_masked varchar(32),
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index if not exists idx_bank_accounts_user_status on bank_accounts(user_id, status);

create table if not exists financial_data (
    id uuid primary key,
    user_id varchar(100) not null,
    status varchar(32) not null,
    last_successful_sync timestamp,
    raw_data text not null,
    created_at timestamp not null
);

create index if not exists idx_financial_data_user_created on financial_data(user_id, created_at desc);

create table if not exists financial_metrics (
    id uuid primary key,
    user_id varchar(100) not null,
    average_monthly_income numeric(18,2) not null,
    income_volatility numeric(10,4) not null,
    debt_to_income_ratio numeric(10,2) not null,
    lowest_monthly_balance numeric(18,2) not null,
    months_of_data integer not null,
    payday_sweep_ratio numeric(10,2) not null,
    created_at timestamp not null
);

create index if not exists idx_financial_metrics_user_created on financial_metrics(user_id, created_at desc);

create table if not exists credit_decisions (
    id uuid primary key,
    user_id varchar(100) not null,
    status varchar(32) not null,
    approved_limit numeric(18,2) not null,
    decision_factors text not null,
    created_at timestamp not null
);

create index if not exists idx_credit_decisions_user_created on credit_decisions(user_id, created_at desc);

create table if not exists credit_certificates (
    id uuid primary key,
    user_id varchar(100) not null,
    approved_amount numeric(18,2) not null,
    status varchar(32) not null,
    expires_at timestamp not null,
    created_at timestamp not null
);

create index if not exists idx_credit_certificates_user_created on credit_certificates(user_id, created_at desc);
