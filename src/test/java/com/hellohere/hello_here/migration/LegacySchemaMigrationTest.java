package com.hellohere.hello_here.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class LegacySchemaMigrationTest {

    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("hello_here")
            .withUsername("test")
            .withPassword("test");

    @Test
    void migratesLegacyLongNamesAndArchivesPostsWithoutLocation() throws SQLException {
        createLegacySchemaAndData();

        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
                .migrate();

        try (Connection connection = openConnection()) {
            assertEquals("あ".repeat(50), queryString(connection,
                    "SELECT name FROM users WHERE id = 1"));
            assertEquals("表".repeat(50), queryString(connection,
                    "SELECT display_name FROM user_profiles WHERE user_id = 1"));

            assertTrue(rowExists(connection, "SELECT 1 FROM posts WHERE id = 1"));
            assertFalse(rowExists(connection, "SELECT 1 FROM posts WHERE id = 2"));
            assertTrue(rowExists(connection,
                    "SELECT 1 FROM migration_archive_posts_missing_location WHERE id = 2"));
            assertTrue(rowExists(connection,
                    "SELECT 1 FROM migration_archive_post_images_missing_location WHERE id = 2 AND post_id = 2"));
            assertNull(queryObject(connection,
                    "SELECT latitude FROM migration_archive_posts_missing_location WHERE id = 2"));
            assertFalse(rowExists(connection, "SELECT 1 FROM post_images WHERE post_id = 2"));

            assertEquals("decimal", queryString(connection, columnMetadataSql("posts", "latitude", "DATA_TYPE")));
            assertEquals("10,7", queryString(connection,
                    columnMetadataSql("posts", "latitude", "NUMERIC_PRECISION, NUMERIC_SCALE")));
            assertEquals("NO", queryString(connection, columnMetadataSql("posts", "latitude", "IS_NULLABLE")));
            assertEquals("3", queryString(connection,
                    "SELECT MAX(version) FROM flyway_schema_history WHERE success = 1"));
        }
    }

    private void createLegacySchemaAndData() throws SQLException {
        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE users (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        email VARCHAR(255) NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        login_failure_count INT NOT NULL DEFAULT 0,
                        locked_until DATETIME NULL,
                        created_at DATETIME NOT NULL,
                        updated_at DATETIME NOT NULL,
                        deleted_at DATETIME NULL,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_users_email (email)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.executeUpdate("""
                    CREATE TABLE user_profiles (
                        user_id BIGINT NOT NULL,
                        display_name VARCHAR(255) NOT NULL,
                        bio VARCHAR(255) NULL,
                        avatar_url VARCHAR(255) NULL,
                        created_at DATETIME NOT NULL,
                        updated_at DATETIME NOT NULL,
                        PRIMARY KEY (user_id),
                        CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.executeUpdate("""
                    CREATE TABLE posts (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        content VARCHAR(255) NULL,
                        latitude DOUBLE NULL,
                        longitude DOUBLE NULL,
                        created_at DATETIME NULL,
                        PRIMARY KEY (id),
                        INDEX idx_posts_user_id (user_id),
                        CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.executeUpdate("""
                    CREATE TABLE post_images (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        post_id BIGINT NOT NULL,
                        image_url VARCHAR(255) NOT NULL,
                        created_at DATETIME NOT NULL,
                        PRIMARY KEY (id),
                        INDEX idx_post_images_post_id (post_id),
                        CONSTRAINT fk_post_images_post FOREIGN KEY (post_id) REFERENCES posts (id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.executeUpdate("CREATE TABLE login_failures (id BIGINT PRIMARY KEY)");
            statement.executeUpdate("CREATE TABLE email_verifications (id BIGINT PRIMARY KEY)");

            statement.executeUpdate("""
                    INSERT INTO users
                        (id, email, name, password_hash, created_at, updated_at)
                    VALUES
                        (1, 'legacy@example.com', '%s', 'hash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """.formatted("あ".repeat(51)));
            statement.executeUpdate("""
                    INSERT INTO user_profiles
                        (user_id, display_name, created_at, updated_at)
                    VALUES
                        (1, '%s', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """.formatted("表".repeat(51)));
            statement.executeUpdate("""
                    INSERT INTO posts
                        (id, user_id, content, latitude, longitude, created_at)
                    VALUES
                        (1, 1, 'valid', 35.681236, 139.767125, CURRENT_TIMESTAMP),
                        (2, 1, 'missing location', NULL, NULL, CURRENT_TIMESTAMP)
                    """);
            statement.executeUpdate("""
                    INSERT INTO post_images (id, post_id, image_url, created_at)
                    VALUES (2, 2, '/uploads/posts/legacy.png', CURRENT_TIMESTAMP)
                    """);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private boolean rowExists(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            return result.next();
        }
    }

    private String queryString(Connection connection, String sql) throws SQLException {
        Object value = queryObject(connection, sql);
        return value == null ? null : value.toString();
    }

    private Object queryObject(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getObject(1);
        }
    }

    private String columnMetadataSql(String tableName, String columnName, String selection) {
        return "SELECT CONCAT_WS(',', " + selection + ") FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + tableName
                + "' AND COLUMN_NAME = '" + columnName + "'";
    }
}
