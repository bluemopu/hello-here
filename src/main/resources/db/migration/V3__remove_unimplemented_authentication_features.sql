DROP TABLE IF EXISTS email_verifications;
DROP TABLE IF EXISTS login_failures;

SET @drop_login_failure_count = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'users'
          AND COLUMN_NAME = 'login_failure_count'
    ),
    'ALTER TABLE users DROP COLUMN login_failure_count',
    'SELECT 1'
);
PREPARE drop_login_failure_count_statement FROM @drop_login_failure_count;
EXECUTE drop_login_failure_count_statement;
DEALLOCATE PREPARE drop_login_failure_count_statement;

SET @drop_locked_until = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'users'
          AND COLUMN_NAME = 'locked_until'
    ),
    'ALTER TABLE users DROP COLUMN locked_until',
    'SELECT 1'
);
PREPARE drop_locked_until_statement FROM @drop_locked_until;
EXECUTE drop_locked_until_statement;
DEALLOCATE PREPARE drop_locked_until_statement;
