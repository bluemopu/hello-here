UPDATE users
SET name = LEFT(name, 50)
WHERE CHAR_LENGTH(name) > 50;

ALTER TABLE users
    MODIFY COLUMN email VARCHAR(255) NOT NULL,
    MODIFY COLUMN name VARCHAR(50) NOT NULL,
    MODIFY COLUMN password_hash VARCHAR(255) NOT NULL,
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL,
    MODIFY COLUMN deleted_at DATETIME NULL;

UPDATE user_profiles
SET display_name = LEFT(display_name, 50)
WHERE CHAR_LENGTH(display_name) > 50;

ALTER TABLE user_profiles
    MODIFY COLUMN display_name VARCHAR(50) NOT NULL,
    MODIFY COLUMN bio TEXT NULL,
    MODIFY COLUMN avatar_url VARCHAR(255) NULL,
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

CREATE TABLE IF NOT EXISTS migration_archive_posts_missing_location LIKE posts;

CREATE TABLE IF NOT EXISTS migration_archive_post_images_missing_location LIKE post_images;

INSERT IGNORE INTO migration_archive_posts_missing_location
SELECT *
FROM posts
WHERE latitude IS NULL OR longitude IS NULL;

INSERT IGNORE INTO migration_archive_post_images_missing_location
SELECT post_images.*
FROM post_images
INNER JOIN posts ON posts.id = post_images.post_id
WHERE posts.latitude IS NULL OR posts.longitude IS NULL;

DELETE post_images
FROM post_images
INNER JOIN posts ON posts.id = post_images.post_id
WHERE posts.latitude IS NULL OR posts.longitude IS NULL;

DELETE FROM posts
WHERE latitude IS NULL OR longitude IS NULL;

UPDATE posts
SET content = ''
WHERE content IS NULL;

UPDATE posts
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL
   OR CAST(created_at AS CHAR) LIKE '0000-00-00%';

ALTER TABLE posts
    MODIFY COLUMN content TEXT NOT NULL,
    MODIFY COLUMN latitude DECIMAL(10, 7) NOT NULL,
    MODIFY COLUMN longitude DECIMAL(10, 7) NOT NULL,
    MODIFY COLUMN created_at DATETIME NOT NULL,
    ADD COLUMN updated_at DATETIME NULL AFTER created_at,
    ADD COLUMN deleted_at DATETIME NULL AFTER updated_at;

ALTER TABLE post_images
    MODIFY COLUMN image_url VARCHAR(255) NOT NULL,
    MODIFY COLUMN created_at DATETIME NOT NULL;
