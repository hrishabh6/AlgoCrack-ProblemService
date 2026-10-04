-- V4__complexity_benchmark_profile.sql — curated complexity benchmark profiles (Problem Service)

CREATE TABLE IF NOT EXISTS `complexity_benchmark_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `profile_id` varchar(64) NOT NULL,
  `profile_code` varchar(64) NOT NULL,
  `question_id` bigint NOT NULL,
  `language` varchar(20) NOT NULL,
  `profile_version` varchar(32) NOT NULL,
  `generator_key` varchar(64) NOT NULL,
  `generator_version` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL,
  `variable_definitions_json` json NOT NULL,
  `size_plan_json` json NOT NULL,
  `variant_plan_json` json NOT NULL,
  `measurement_limits_json` json NOT NULL,
  `profile_hash` char(64) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_complexity_profile_id` (`profile_id`),
  UNIQUE KEY `uk_complexity_question_lang_version` (`question_id`, `language`, `profile_version`),
  KEY `idx_complexity_question_lang_status` (`question_id`, `language`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
