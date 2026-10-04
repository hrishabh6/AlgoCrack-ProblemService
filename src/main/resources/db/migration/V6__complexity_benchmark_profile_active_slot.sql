-- V6: at most one active benchmark profile per (question_id, language).
-- active_slot is derived from status; status is the writable lifecycle field.

ALTER TABLE complexity_benchmark_profile
  ADD COLUMN active_slot tinyint
  GENERATED ALWAYS AS (
    CASE
      WHEN status = 'ACTIVE' THEN 1
      ELSE NULL
    END
  ) STORED
  AFTER status;

ALTER TABLE complexity_benchmark_profile
  ADD UNIQUE KEY uk_complexity_question_lang_active_slot (question_id, language, active_slot);
