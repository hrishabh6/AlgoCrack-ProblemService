-- V1__init_problem_schema.sql
-- ProblemService schema — owns question, tag, test_case, solution, reference_solution, question_metadata
-- Generated from: mysqldump --no-data leetcode (2026-03-05)
-- Note: All intra-service FKs preserved. No cross-service FKs.

-- question (no FKs to other services)
CREATE TABLE IF NOT EXISTS `question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `question_title` varchar(255) DEFAULT NULL,
  `question_description` text,
  `is_output_order_matters` tinyint(1) DEFAULT NULL,
  `difficulty_level` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `constraints` text,
  `timeout_limit` int DEFAULT NULL,
  `node_type` enum('GRAPH_NODE','LIST_NODE','TREE_NODE') DEFAULT NULL,
  `validation_hints` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- tag
CREATE TABLE IF NOT EXISTS `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- question_tag (join table, FKs to question and tag — both in this service)
CREATE TABLE IF NOT EXISTS `question_tag` (
  `question_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`question_id`,`tag_id`),
  KEY `tag_id` (`tag_id`),
  CONSTRAINT `question_tag_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
  CONSTRAINT `question_tag_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- test_case (FK to question — same service)
CREATE TABLE IF NOT EXISTS `test_case` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `question_id` bigint DEFAULT NULL,
  `input` text,
  `expected_output` text,
  `order_index` int DEFAULT NULL,
  `is_hidden` tinyint(1) DEFAULT NULL,
  `type` enum('DEFAULT','HIDDEN') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `question_id` (`question_id`),
  CONSTRAINT `test_case_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- solution (FK to question — same service)
CREATE TABLE IF NOT EXISTS `solution` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `question_id` bigint NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `code` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_unique_question_language` (`question_id`,`language`),
  CONSTRAINT `fk_solution_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- reference_solution (FK to question — same service)
CREATE TABLE IF NOT EXISTS `reference_solution` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `language` enum('CPP','JAVA','JAVASCRIPT','PYTHON') NOT NULL,
  `source_code` text NOT NULL,
  `question_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe101b35ir3smvku0dexx1ptrc` (`question_id`),
  CONSTRAINT `FK1wra1dhniujbcvs4xqq8aq6n6` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- question_metadata (FK to question — same service)
CREATE TABLE IF NOT EXISTS `question_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `function_name` varchar(255) DEFAULT NULL,
  `return_type` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `code_template` text,
  `test_case_format` text,
  `execution_strategy` varchar(255) DEFAULT NULL,
  `custom_input_enabled` bit(1) DEFAULT NULL,
  `question_id` bigint DEFAULT NULL,
  `mutation_target` varchar(255) DEFAULT NULL,
  `serialization_strategy` varchar(255) DEFAULT NULL,
  `question_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_QUESTIONMETADATA_ON_QUESTION` (`question_id`),
  CONSTRAINT `FK_QUESTIONMETADATA_ON_QUESTION` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- question_metadata_param_names (FK to question_metadata — same service)
CREATE TABLE IF NOT EXISTS `question_metadata_param_names` (
  `question_metadata_id` bigint NOT NULL,
  `param_names` varchar(255) DEFAULT NULL,
  KEY `fk_questionmetadata_paramnames_on_question_metadata` (`question_metadata_id`),
  CONSTRAINT `fk_questionmetadata_paramnames_on_question_metadata` FOREIGN KEY (`question_metadata_id`) REFERENCES `question_metadata` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- question_metadata_param_types (FK to question_metadata — same service)
CREATE TABLE IF NOT EXISTS `question_metadata_param_types` (
  `question_metadata_id` bigint NOT NULL,
  `param_types` varchar(255) DEFAULT NULL,
  KEY `fk_questionmetadata_paramtypes_on_question_metadata` (`question_metadata_id`),
  CONSTRAINT `fk_questionmetadata_paramtypes_on_question_metadata` FOREIGN KEY (`question_metadata_id`) REFERENCES `question_metadata` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
