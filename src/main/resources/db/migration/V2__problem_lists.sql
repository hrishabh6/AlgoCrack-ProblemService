-- V2__problem_lists.sql
-- User-owned problem organisation: the built-in "Saved" collection and custom problem lists.
-- user_id is the AuthService business id (JWT `userId` claim). Users live in another database,
-- so there is intentionally no FK on user_id (same convention as submission.user_id).
-- Name uniqueness is case-insensitive because of the utf8mb4_0900_ai_ci collation.

CREATE TABLE IF NOT EXISTS `saved_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `question_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_saved_problem_user_question` (`user_id`, `question_id`),
  KEY `idx_saved_problem_question` (`question_id`),
  CONSTRAINT `fk_saved_problem_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `problem_list` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `name` varchar(60) NOT NULL,
  `description` varchar(280) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_problem_list_user_name` (`user_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `problem_list_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `list_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_problem_list_item_list_question` (`list_id`, `question_id`),
  KEY `idx_problem_list_item_question` (`question_id`),
  CONSTRAINT `fk_problem_list_item_list` FOREIGN KEY (`list_id`) REFERENCES `problem_list` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_problem_list_item_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
