-- V7: problem-level semantic profiles. One ACTIVE row per question.
-- active_slot follows the complexity benchmark profile pattern.

CREATE TABLE IF NOT EXISTS `semantic_problem_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `profile_id` varchar(64) NOT NULL,
  `question_id` bigint NOT NULL,
  `semantic_schema_version` varchar(32) NOT NULL,
  `semantic_version` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL,
  `active_slot` tinyint
    GENERATED ALWAYS AS (
      CASE
        WHEN status = 'ACTIVE' THEN 1
        ELSE NULL
      END
    ) STORED,
  `profile_json` json NOT NULL,
  `profile_hash` char(64) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_semantic_profile_id` (`profile_id`),
  UNIQUE KEY `uk_semantic_question_version` (`question_id`, `semantic_version`),
  UNIQUE KEY `uk_semantic_question_active_slot` (`question_id`, `active_slot`),
  KEY `idx_semantic_question_status` (`question_id`, `status`),
  CONSTRAINT `fk_semantic_profile_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
