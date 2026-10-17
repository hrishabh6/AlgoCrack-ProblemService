-- V8: backfill Semantic Metadata V1 for seeded questions that already exist.
-- Rows are skipped when the question is absent, so an empty Flyway database stays valid.
-- Java compatibility is the activation target. Profiles describe input shape only.

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q1-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["MATRIX"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"n","kind":"DIMENSION","binding":{"parameterIndex":0,"dimension":"ROWS"}},{"name":"m","kind":"DIMENSION","binding":{"parameterIndex":0,"dimension":"COLUMNS"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"MATRIX","elementKind":"INT","encoding":"JSON_NESTED_ARRAY","constraints":{"rectangular":true}}],"mutatedInputs":[]}' AS JSON),
  '726bd79d72f7213857321fd82040090cfa019b97215fb1bf0771428108f38da1',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 1
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q1-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q2-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["LINKED_LIST"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"n","kind":"COUNT","binding":{"parameterIndex":0,"dimension":"NODE_COUNT"}},{"name":"k","kind":"COUNT","binding":{"parameterIndex":1,"dimension":"SCALAR"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"LIST_NODE","encoding":"JSON_ARRAY","constraints":{"singlyLinked":true}}],"mutatedInputs":[]}' AS JSON),
  '9997fcda00202f2e5a7b883f09b5986388ff6a57258a4ee3be3cd2918019d907',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 2
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q2-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q3-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["GRAPH"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"v","kind":"COUNT","binding":{"parameterIndex":0,"dimension":"SCALAR"}},{"name":"e","kind":"COUNT","binding":{"parameterIndex":1,"dimension":"EDGE_COUNT"}}],"structures":[{"binding":{"parameterIndex":1},"kind":"EDGE_LIST","elementKind":"INT","encoding":"EDGE_PAIRS","constraints":{"undirected":true,"zeroBasedVertexLabels":true}}],"mutatedInputs":[]}' AS JSON),
  '8fc0dab66963cabe1ab860d0c3c31ce6a3c283326362b811802e6128b0bd6856',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 3
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q3-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q4-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["BINARY_TREE","DESIGN_CLASS","MULTI_ENTRY"],"entry":{"kind":"DESIGN_CLASS","className":"Codec","inputEncoding":"OPERATION_TRACE","operations":[{"name":"Codec","role":"CONSTRUCTOR","parameters":[]},{"name":"serialize","role":"METHOD","parameters":[{"kind":"TREE_NODE","encoding":"LEVEL_ORDER"}],"returns":"STRING"},{"name":"deserialize","role":"METHOD","parameters":[{"kind":"STRING"}],"returns":"TREE_NODE"}]},"sizeVariables":[{"name":"n","kind":"COUNT","binding":{"operationName":"serialize","operationParameterIndex":0,"dimension":"NODE_COUNT"}},{"name":"s","kind":"LENGTH","binding":{"operationName":"deserialize","operationParameterIndex":0,"dimension":"LENGTH"}}],"structures":[],"mutatedInputs":[]}' AS JSON),
  'e1f30738d1e044873c6e83684225c5343b332235d53ffec72acb804aec3e5625',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 4
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q4-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q5-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["STRING","ARRAY"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"n","kind":"LENGTH","binding":{"parameterIndex":0,"dimension":"LENGTH"}},{"name":"d","kind":"COUNT","binding":{"parameterIndex":1,"dimension":"LENGTH"}},{"name":"L","kind":"AGGREGATE_LENGTH","binding":{"parameterIndex":1,"dimension":"LENGTH"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"STRING","elementKind":"CHAR"},{"binding":{"parameterIndex":1},"kind":"ARRAY","elementKind":"STRING","encoding":"JSON_ARRAY"}],"mutatedInputs":[]}' AS JSON),
  '0839233137428eb14993972dd0df1530ebb03fba1f3c138aad9e89675d46f774',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 5
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q5-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q6-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["BINARY_TREE","MUTATED_INPUT"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"n","kind":"COUNT","binding":{"parameterIndex":0,"dimension":"NODE_COUNT"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"TREE_NODE","encoding":"LEVEL_ORDER"}],"mutatedInputs":[{"parameterIndex":0,"structureKind":"TREE_NODE"}]}' AS JSON),
  'a3f0318c19ede6d3295cd96aae5f64c364f74a7569c005c160b8a269bb771a85',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 6
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q6-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q7-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["LINKED_LIST","MUTATED_INPUT"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"n","kind":"COUNT","binding":{"parameterIndex":0,"dimension":"NODE_COUNT"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"LIST_NODE","encoding":"JSON_ARRAY","constraints":{"singlyLinked":true}}],"mutatedInputs":[{"parameterIndex":0,"structureKind":"LIST_NODE"}]}' AS JSON),
  'f15d0c0ae63abbede0716049e29a4506c1fcb1dd5f3a79bf368eb5e1ef679081',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 7
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q7-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q8-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["GRAPH"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"v","kind":"COUNT","binding":{"parameterIndex":0,"dimension":"NODE_COUNT"}},{"name":"e","kind":"COUNT","binding":{"parameterIndex":0,"dimension":"EDGE_COUNT"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"GRAPH_NODE","encoding":"ADJACENCY_LIST","constraints":{"oneIndexedEntryNode":true}}],"mutatedInputs":[]}' AS JSON),
  '11ee4c5127e957d1dbed8905fc3e79b6a500851e7789705d6d8323b4300e8cd6',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 8
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q8-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q9-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["MATRIX","MUTATED_INPUT"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"r","kind":"DIMENSION","binding":{"parameterIndex":0,"dimension":"ROWS"}},{"name":"c","kind":"DIMENSION","binding":{"parameterIndex":0,"dimension":"COLUMNS"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"CHAR_MATRIX","elementKind":"CHAR","encoding":"JSON_NESTED_ARRAY","constraints":{"rectangular":true,"fixedRows":9,"fixedColumns":9}}],"mutatedInputs":[{"parameterIndex":0,"structureKind":"CHAR_MATRIX"}]}' AS JSON),
  '780cd4cae6ed4808237d15df7bf362e48f8cf967de406b1f59ff37bd15be9300',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 9
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q9-v1'
  );

INSERT INTO semantic_problem_profile (
  profile_id, question_id, semantic_schema_version, semantic_version, status,
  profile_json, profile_hash, created_at, updated_at
)
SELECT
  'spp-q10-v1', q.id, 'semantic-v1', 'v1', 'ACTIVE',
  CAST('{"schemaVersion":"semantic-v1","families":["ARRAY"],"entry":{"kind":"FUNCTION"},"sizeVariables":[{"name":"n","kind":"LENGTH","binding":{"parameterIndex":0,"dimension":"LENGTH"}}],"structures":[{"binding":{"parameterIndex":0},"kind":"ARRAY","elementKind":"INT","encoding":"JSON_ARRAY"}],"mutatedInputs":[]}' AS JSON),
  '1daeb49ff5ecb1c7c7605da6a753bf63266b57556a962fa6aec9b10bb2b97338',
  NOW(6), NOW(6)
FROM question q
WHERE q.id = 10
  AND NOT EXISTS (
    SELECT 1 FROM semantic_problem_profile existing WHERE existing.profile_id = 'spp-q10-v1'
  );
