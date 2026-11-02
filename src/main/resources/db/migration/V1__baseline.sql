-- 운영 DB 기준선 스키마 (V2 적용 전 상태, mysqldump --no-data 기반)
-- 운영은 baseline-version=1 이라 이 파일을 실행하지 않는다. 빈 DB(CI/로컬)에서만 적용된다.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `attachments` (
  `type` tinyint DEFAULT NULL,
  `attachment_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `display_order` bigint NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `post_id` bigint NOT NULL,
  `file_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `storage_key` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stored_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `FKpeivhpes6blw5rflv2ty65erl` (`post_id`),
  CONSTRAINT `FKpeivhpes6blw5rflv2ty65erl` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`),
  CONSTRAINT `attachments_chk_1` CHECK ((`type` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comments` (
  `is_deleted` bit(1) NOT NULL,
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `parent_comment_id` bigint DEFAULT NULL,
  `post_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `visibility` enum('PRIVATE','PUBLIC') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `FK7h839m3lkvhbyv3bcdv7sm4fj` (`parent_comment_id`),
  KEY `FKh4c7lvsc298whoyd4w9ta25cr` (`post_id`),
  KEY `FK8omq0tc18jd43bu5tjh6jvraq` (`user_id`),
  CONSTRAINT `FK7h839m3lkvhbyv3bcdv7sm4fj` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`comment_id`),
  CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKh4c7lvsc298whoyd4w9ta25cr` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `crawled_project` (
  `crawled_project_id` bigint NOT NULL AUTO_INCREMENT,
  `source_uid` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `term` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` text COLLATE utf8mb4_unicode_ci,
  `description` text COLLATE utf8mb4_unicode_ci,
  `content` text COLLATE utf8mb4_unicode_ci,
  `source_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `presentation_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `video_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `github_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `representative_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`crawled_project_id`),
  UNIQUE KEY `uk_crawled_project_source` (`source_uid`,`term`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `crawled_project_member` (
  `crawled_project_member_id` bigint NOT NULL AUTO_INCREMENT,
  `crawled_project_id` bigint NOT NULL,
  `role` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `masked_email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email_prefix` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `grade` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`crawled_project_member_id`),
  KEY `idx_crawled_project_member_project` (`crawled_project_id`),
  KEY `idx_crawled_project_member_match` (`name`,`email_prefix`),
  CONSTRAINT `fk_crawled_project_member_project` FOREIGN KEY (`crawled_project_id`) REFERENCES `crawled_project` (`crawled_project_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `keywords` (
  `created_at` datetime(6) NOT NULL,
  `keyword_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`keyword_id`),
  UNIQUE KEY `UKc99dtunbvho2mop5nykxuo6x4` (`keyword_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `likes` (
  `created_at` datetime(6) NOT NULL,
  `like_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `UK2jovqhqo324cubdomovkex03b` (`post_id`,`user_id`),
  KEY `FKnvx9seeqqyy71bij291pwiwrg` (`user_id`),
  CONSTRAINT `FKnvx9seeqqyy71bij291pwiwrg` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKry8tnr4x2vwemv2bb0h5hyl0x` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notifications` (
  `email_sent` bit(1) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) DEFAULT NULL,
  `reference_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `message` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reference_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notification_type` enum('ADMIN_NOTICE','APPROVAL','COMMENT','LIKE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `FK9y21adhxn0ayjhfocscqox7bh` (`user_id`),
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `post_keywords` (
  `created_at` datetime(6) NOT NULL,
  `keyword_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `post_keyword_id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`post_keyword_id`),
  UNIQUE KEY `UKd9rhjigtmexg59t6kg5shjecb` (`post_id`,`keyword_id`),
  KEY `FKmvvvdh21hn9m6iimlmr10d4n3` (`keyword_id`),
  CONSTRAINT `FKfx77c3lbglc6p1okopo1ooijr` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`),
  CONSTRAINT `FKmvvvdh21hn9m6iimlmr10d4n3` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`keyword_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `posts` (
  `comment_count` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `like_count` bigint NOT NULL,
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `view_count` bigint NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `github_link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `thumbnail_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `thumbnail_storage_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `video_link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `board_type` enum('COMPANY_PROJECT','CRAWLED_PROJECT','LAB_INTERN','NOTICE','PORTFOLIO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `visibility` enum('PRIVATE','PUBLIC') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`post_id`),
  KEY `FK5lidm6cqbc7u4xhqpxm898qme` (`user_id`),
  CONSTRAINT `FK5lidm6cqbc7u4xhqpxm898qme` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `admin_role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NONE',
  `created_at` datetime(6) NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `department` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `firebase_uid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `profile_bio` text COLLATE utf8mb4_unicode_ci,
  `profile_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_role` enum('COMPANY','PROFESSOR','STUDENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_status` enum('ACTIVE','BLOCKED','PENDING','SUSPENDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK4yiq7pdiwjw8inhg1xy83nabx` (`firebase_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
