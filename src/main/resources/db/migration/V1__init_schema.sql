-- =============================================================================
-- V1__init_schema.sql
-- Initial schema for OrgManager: departments, users, documents, approvals, audit
-- =============================================================================

-- ── Departments ──────────────────────────────────────────────────────────────
-- head_id FK is added after users table to avoid circular dependency
CREATE TABLE departments (
    id      BIGSERIAL    PRIMARY KEY,
    name    VARCHAR(255) NOT NULL
);

-- ── Users / Employees ────────────────────────────────────────────────────────
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    position      VARCHAR(255),
    department_id BIGINT       REFERENCES departments (id) ON DELETE SET NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE')),
    status        VARCHAR(50)  NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- Back-reference: department head
ALTER TABLE departments
    ADD COLUMN head_id BIGINT REFERENCES users (id) ON DELETE SET NULL;

-- ── Documents ────────────────────────────────────────────────────────────────
CREATE TABLE documents (
    id            BIGSERIAL     PRIMARY KEY,
    title         VARCHAR(500)  NOT NULL,
    file_path     VARCHAR(1000),
    author_id     BIGINT        NOT NULL REFERENCES users (id),
    document_type VARCHAR(100)  NOT NULL,
    version       INTEGER       NOT NULL DEFAULT 1,
    status        VARCHAR(50)   NOT NULL
        CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED'))
        DEFAULT 'DRAFT',
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ── Document Approvals ───────────────────────────────────────────────────────
CREATE TABLE document_approvals (
    id          BIGSERIAL   PRIMARY KEY,
    document_id BIGINT      NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    approver_id BIGINT      NOT NULL REFERENCES users (id),
    decision    VARCHAR(50) NOT NULL
        CHECK (decision IN ('PENDING', 'APPROVED', 'REJECTED'))
        DEFAULT 'PENDING',
    comment     TEXT,
    decided_at  TIMESTAMP
);

-- ── Audit Logs ───────────────────────────────────────────────────────────────
CREATE TABLE audit_logs (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    action      VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    details     TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- Indexes
-- =============================================================================
CREATE INDEX idx_users_email        ON users (email);
CREATE INDEX idx_users_dept         ON users (department_id);
CREATE INDEX idx_users_role         ON users (role);
CREATE INDEX idx_users_status       ON users (status);

CREATE INDEX idx_docs_author        ON documents (author_id);
CREATE INDEX idx_docs_status        ON documents (status);
CREATE INDEX idx_docs_created       ON documents (created_at DESC);

CREATE INDEX idx_approvals_doc      ON document_approvals (document_id);
CREATE INDEX idx_approvals_approver ON document_approvals (approver_id);
CREATE INDEX idx_approvals_decision ON document_approvals (decision);

CREATE INDEX idx_audit_user         ON audit_logs (user_id);
CREATE INDEX idx_audit_entity       ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_created      ON audit_logs (created_at DESC);

-- =============================================================================
-- Seed data — default admin (password = admin123, BCrypt cost 12)
-- =============================================================================
INSERT INTO users (full_name, position, email, password_hash, role, status)
VALUES ('System Administrator',
        'Admin',
        'admin@orgmanager.kz',
        '$2a$12$Jt6GRUxY6IFhDK5BdZ7MAuO9Ysl8lXb1TXAMBpjJR3.5ZkVTkfA9W',
        'ADMIN',
        'ACTIVE');
