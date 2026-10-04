-- Curated JAVA profiles for verified catalog questions (IDs from live schema dumps).
-- Safe to re-run: uses profile_id uniqueness; rows are immutable semantics per version.

INSERT INTO complexity_benchmark_profile (
  profile_id, profile_code, question_id, language, profile_version,
  generator_key, generator_version, status,
  variable_definitions_json, size_plan_json, variant_plan_json, measurement_limits_json,
  profile_hash, created_at, updated_at
) VALUES
(
  'cbp-q1-java-v1',
  'TRAP_RAIN_MATRIX',
  1,
  'JAVA',
  'v1',
  'INT_MATRIX_ROWS_COLS',
  'v1',
  'ACTIVE',
  JSON_ARRAY(
    JSON_OBJECT('name', 'n', 'meaning', 'rows of heightMap', 'parameter', 'heightMap', 'dimension', 'rows'),
    JSON_OBJECT('name', 'm', 'meaning', 'columns of heightMap', 'parameter', 'heightMap', 'dimension', 'cols')
  ),
  JSON_OBJECT(
    'ladder', JSON_OBJECT('n', JSON_ARRAY(8, 16, 32), 'm', JSON_ARRAY(8, 16, 32)),
    'maxSizes', JSON_OBJECT('n', 64, 'm', 64)
  ),
  JSON_OBJECT('variants', JSON_ARRAY('RANDOM', 'FLAT', 'PEAK')),
  JSON_OBJECT('warmups', 3, 'measuredRepeats', 5, 'perInvocationTimeoutMs', 2000, 'maxTotalProfileMs', 20000),
  '9318de79c2efbdb1b00e72b4b54a13c42e63bc482c3136fdfd60ee12a06dadb1',
  NOW(6),
  NOW(6)
),
(
  'cbp-q3-java-v1',
  'CRITICAL_CONNECTIONS_GRAPH',
  3,
  'JAVA',
  'v1',
  'UNDIRECTED_GRAPH_EDGES',
  'v1',
  'ACTIVE',
  JSON_ARRAY(
    JSON_OBJECT('name', 'v', 'meaning', 'number of nodes', 'parameter', 'n'),
    JSON_OBJECT('name', 'e', 'meaning', 'number of edges', 'parameter', 'connections', 'dimension', 'edgeCount')
  ),
  JSON_OBJECT(
    'ladder', JSON_OBJECT('v', JSON_ARRAY(32, 64), 'e', JSON_ARRAY(48, 96)),
    'maxSizes', JSON_OBJECT('v', 128, 'e', 256)
  ),
  JSON_OBJECT('variants', JSON_ARRAY('SPARSE', 'CHAIN', 'RANDOM')),
  JSON_OBJECT('warmups', 3, 'measuredRepeats', 5, 'perInvocationTimeoutMs', 2000, 'maxTotalProfileMs', 20000),
  '72f2b1a01bbd0b0b00c85d32ec06088dd2ed045a430452b1b687623973660075',
  NOW(6),
  NOW(6)
),
(
  'cbp-q10-java-v1',
  'FOUR_SUM_INT_ARRAY',
  10,
  'JAVA',
  'v1',
  'INT_ARRAY_WITH_TARGET',
  'v1',
  'ACTIVE',
  JSON_ARRAY(
    JSON_OBJECT('name', 'n', 'meaning', 'length of nums', 'parameter', 'nums', 'dimension', 'length')
  ),
  JSON_OBJECT(
    'ladder', JSON_OBJECT('n', JSON_ARRAY(64, 128, 256)),
    'maxSizes', JSON_OBJECT('n', 512)
  ),
  JSON_OBJECT('variants', JSON_ARRAY('RANDOM', 'SORTED', 'ADVERSARIAL')),
  JSON_OBJECT('warmups', 3, 'measuredRepeats', 5, 'perInvocationTimeoutMs', 1000, 'maxTotalProfileMs', 15000),
  '18a8ce82859f2c884b71c98eccc46c43c24467c68c92659c369482120e5efd10',
  NOW(6),
  NOW(6)
)
ON DUPLICATE KEY UPDATE updated_at = updated_at;
