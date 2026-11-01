-- #57: CrawlStatus 엔티티 / Post.crawledProjectId (b86586b)
-- 이미 수동 적용된 DB에서도 안전하도록 존재 여부를 확인한다.
CREATE TABLE IF NOT EXISTS crawl_status (
    id BIGINT NOT NULL,
    last_crawled_at DATETIME(6) NULL,
    PRIMARY KEY (id)
);

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'posts' AND COLUMN_NAME = 'crawled_project_id') = 0,
    'ALTER TABLE posts ADD COLUMN crawled_project_id BIGINT NULL',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
