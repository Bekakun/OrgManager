-- =============================================================================
-- reset_db.sql — Очистка базы данных OrgManager
-- Оставляет только System Administrator, удаляет все остальные данные.
--
-- Способ 1 — через pgAdmin:
--   Откройте Query Tool (Alt+Shift+Q) и вставьте этот скрипт.
--
-- Способ 2 — через Docker:
--   docker exec -i orgmanager-db psql -U postgres -d orgmanager < scripts/reset_db.sql
-- =============================================================================

BEGIN;

-- 1. Очистить журнал аудита
TRUNCATE TABLE audit_logs RESTART IDENTITY CASCADE;

-- 2. Удалить согласования документов
TRUNCATE TABLE document_approvals RESTART IDENTITY CASCADE;

-- 3. Удалить все документы
TRUNCATE TABLE documents RESTART IDENTITY CASCADE;

-- 4. Удалить всех сотрудников КРОМЕ System Administrator
DELETE FROM users
WHERE email <> 'admin@orgmanager.kz';

-- 5. Удалить все отделы
--    (head_id FK обнуляется автоматически через ON DELETE SET NULL)
TRUNCATE TABLE departments RESTART IDENTITY CASCADE;

-- 6. Сбросить счётчик ID у users (опционально — пересчитать с текущего max)
SELECT setval(pg_get_serial_sequence('users', 'id'), MAX(id)) FROM users;

COMMIT;

-- Проверка результата
SELECT id, full_name, email, role, status FROM users;
