-- POTD / daily challenge schema (UTC calendar dates)

ALTER TABLE `question`
  ADD COLUMN `status` varchar(20) NOT NULL DEFAULT 'PUBLISHED' AFTER `validation_hints`;

CREATE INDEX `idx_question_status` ON `question` (`status`);

CREATE TABLE IF NOT EXISTS `potd_problem_metadata` (
  `question_id` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eligible` tinyint(1) NOT NULL DEFAULT 0,
  `curation_status` varchar(20) NOT NULL DEFAULT 'NEEDS_REVIEW',
  `quality_score` int NOT NULL DEFAULT 0,
  `primary_tag_id` bigint DEFAULT NULL,
  `family_key` varchar(128) DEFAULT NULL,
  `cooldown_days_override` int DEFAULT NULL,
  `validation_status` varchar(20) NOT NULL DEFAULT 'NOT_VALIDATED',
  `validated_at` timestamp NULL DEFAULT NULL,
  `reviewed_at` timestamp NULL DEFAULT NULL,
  `reviewed_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`question_id`),
  CONSTRAINT `fk_potd_meta_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_potd_meta_primary_tag` FOREIGN KEY (`primary_tag_id`) REFERENCES `tag` (`id`),
  CONSTRAINT `chk_potd_quality` CHECK (`quality_score` >= 0 AND `quality_score` <= 100),
  CONSTRAINT `chk_potd_cooldown_override` CHECK (`cooldown_days_override` IS NULL OR `cooldown_days_override` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX `idx_potd_meta_eligibility` ON `potd_problem_metadata` (`eligible`, `curation_status`, `validation_status`);
CREATE INDEX `idx_potd_meta_primary_tag` ON `potd_problem_metadata` (`primary_tag_id`);

CREATE TABLE IF NOT EXISTS `daily_challenge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `challenge_date` date NOT NULL,
  `question_id` bigint NOT NULL,
  `status` varchar(20) NOT NULL,
  `selection_type` varchar(20) NOT NULL,
  `locked` tinyint(1) NOT NULL DEFAULT 0,
  `scheduler_version` varchar(64) NOT NULL,
  `configuration_hash` varchar(64) NOT NULL,
  `deterministic_seed` varchar(128) NOT NULL,
  `selection_score` decimal(10,6) DEFAULT NULL,
  `selection_reason` json DEFAULT NULL,
  `published_at` timestamp NULL DEFAULT NULL,
  `published_by` varchar(255) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_challenge_date` (`challenge_date`),
  KEY `idx_daily_challenge_status_date` (`status`, `challenge_date`),
  KEY `idx_daily_challenge_question_date` (`question_id`, `challenge_date`),
  KEY `idx_daily_challenge_locked` (`locked`, `status`, `challenge_date`),
  CONSTRAINT `fk_daily_challenge_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `daily_challenge_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `challenge_id` bigint DEFAULT NULL,
  `challenge_date` date NOT NULL,
  `action` varchar(32) NOT NULL,
  `old_question_id` bigint DEFAULT NULL,
  `new_question_id` bigint DEFAULT NULL,
  `actor` varchar(255) DEFAULT NULL,
  `reason` varchar(512) DEFAULT NULL,
  `request_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dca_challenge_created` (`challenge_id`, `created_at`),
  KEY `idx_dca_date_created` (`challenge_date`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
