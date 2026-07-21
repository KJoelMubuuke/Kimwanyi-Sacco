-- ============================================================
-- Kimwanyi SACCO Management System — Database Schema
-
-- ============================================================

CREATE DATABASE IF NOT EXISTS kimwanyi_sacco
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE kimwanyi_sacco;

-- ------------------------------------------------------------
-- 1. MEMBERS
-- Rule: unique National ID / membership number.
-- Rule: deactivated, never deleted -> status flag, no hard delete.
-- ------------------------------------------------------------
CREATE TABLE members (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    national_id          VARCHAR(30)  NOT NULL UNIQUE,
    
    full_name             VARCHAR(120) NOT NULL,
    phone_number           VARCHAR(20),
    email                   VARCHAR(120),
    password_hash           VARCHAR(255) NOT NULL,
    status                   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_member_status
        CHECK (status IN ('ACTIVE', 'DEACTIVATED'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. ADMINS
-- Rule: admin accounts are structurally separate from members
-- (own table, own login, no FK to members).
-- ------------------------------------------------------------
CREATE TABLE admin (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(60)  NOT NULL UNIQUE,
    full_name             VARCHAR(120) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. SAVINGS_ACCOUNTS (1:1 with member)
-- balance is CACHED (kept in sync inside the same DB transaction
-- as each transactions insert) -> O(1) reads instead of SUM().
-- version column = optimistic locking, guards against two
-- concurrent writes (e.g. deposit + withdrawal racing) silently
-- overwriting each other.
-- ------------------------------------------------------------
CREATE TABLE savings_accounts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id             BIGINT NOT NULL UNIQUE,
    balance                 DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    version                  BIGINT NOT NULL DEFAULT 0,
    created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_savings_member
        FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT chk_savings_balance_nonnegative
        CHECK (balance >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. TRANSACTIONS (deposit / withdrawal ledger)
-- Immutable, append-only. balance_after is stamped at insert
-- time so a member statement is a single indexed range scan,
-- not a running recomputation.
-- ------------------------------------------------------------
CREATE TABLE transactions (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    savings_account_id       BIGINT NOT NULL,
    type                       VARCHAR(20) NOT NULL,
    amount                      DECIMAL(19,2) NOT NULL,
    balance_after                DECIMAL(19,2) NOT NULL,
    description                   VARCHAR(255),
    created_at                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_savings_account
        FOREIGN KEY (savings_account_id) REFERENCES savings_accounts(id),
    CONSTRAINT chk_txn_type
        CHECK (type IN ('DEPOSIT', 'WITHDRAWAL')),
    CONSTRAINT chk_txn_amount_positive
        CHECK (amount > 0),
    INDEX idx_txn_account_date (savings_account_id, created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. LOAN_APPLICATIONS
-- Rule: only an admin can approve/reject.
-- Rule (part 1 of "one active loan"): a member cannot have more
-- than one application sitting at PENDING at a time -> enforced
-- in the service layer via idx_application_member_status below,
-- checked alongside loans.status (see table 6).
-- ------------------------------------------------------------
CREATE TABLE loan_applications (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id                 BIGINT NOT NULL,
    requested_amount            DECIMAL(19,2) NOT NULL,
    reason                        VARCHAR(255),
    status                         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at                      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at                      DATETIME NULL,
    reviewed_by                       BIGINT NULL,
    rejection_reason                    VARCHAR(255),
    CONSTRAINT fk_application_member
        FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT fk_application_reviewer
        FOREIGN KEY (reviewed_by) REFERENCES admin(id),
    CONSTRAINT chk_application_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_application_amount_positive
        CHECK (requested_amount > 0),
    INDEX idx_application_member_status (member_id, status)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. LOANS
-- Only ever created from an APPROVED loan_application (1:1 via
-- loan_application_id UNIQUE). member_id is a denormalized
-- shortcut for fast lookups -> must always equal
-- loan_applications.member_id (enforce in the service layer
-- when inserting).
-- Rule (part 2 of "one active loan"): status here is checked
-- together with loan_applications.status in the service layer
-- before a new application is allowed.
-- Rule: must be fully repaid before a new loan can be applied for.
-- approved_by / approved_at duplicate loan_applications.reviewed_by /
-- reviewed_at. This is intentional denormalization for fast reads
-- on the loan itself, but loan_applications stays the source of
-- truth -> never update these two columns independently.
-- ------------------------------------------------------------
CREATE TABLE loans (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_application_id       BIGINT NOT NULL UNIQUE,
    member_id                   BIGINT NOT NULL,
    principal                     DECIMAL(19,2) NOT NULL,
    interest_amount                 DECIMAL(19,2) NOT NULL,
    total_repayable                   DECIMAL(19,2) NOT NULL,
    remaining_balance                   DECIMAL(19,2) NOT NULL,
    status                                 VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    approved_by                             BIGINT NOT NULL,
    approved_at                               DATETIME NOT NULL,
    CONSTRAINT fk_loan_application
        FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id),
    CONSTRAINT fk_loan_member
        FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT fk_loan_approver
        FOREIGN KEY (approved_by) REFERENCES admin(id),
    CONSTRAINT chk_loan_status
        CHECK (status IN ('ACTIVE', 'REPAID')),
    CONSTRAINT chk_loan_principal_positive
        CHECK (principal > 0),
    CONSTRAINT chk_loan_remaining_nonnegative
        CHECK (remaining_balance >= 0),
    INDEX idx_loan_member_status (member_id, status)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. LOAN_REPAYMENTS
-- Each insert updates loans.remaining_balance in the same DB
-- transaction, flipping loans.status to REPAID once
-- remaining_balance reaches 0 (service-layer logic, kept out of
-- triggers so all business rules live in one place).
-- ------------------------------------------------------------
CREATE TABLE loan_repayments (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id                   BIGINT NOT NULL,
    amount                      DECIMAL(19,2) NOT NULL,
    paid_at                       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_repayment_loan
        FOREIGN KEY (loan_id) REFERENCES loans(id),
    CONSTRAINT chk_repayment_amount_positive
        CHECK (amount > 0),
    INDEX idx_repayment_loan (loan_id)
) ENGINE=InnoDB;
