package com.hrishabh.problemservice.complexity.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.VariableDefinition;
import com.hrishabh.problemservice.complexity.service.ComplexityProfileValidator;
import com.hrishabh.problemservice.complexity.service.ComplexityProfileValidator.MeasurementLimits;
import com.hrishabh.problemservice.complexity.support.ProfileCanonicalJson;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Requires Docker for Testcontainers. Skipped when Docker is unavailable to the test JVM.
 */
class ComplexityBenchmarkProfileFlywayIntegrationTest {

    private static MySQLContainer<?> mysql;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void startDatabaseAndMigrate() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker unavailable — skipping MySQL Flyway integration");
        mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("algocrack_problem_test")
                .withUsername("test")
                .withPassword("test");
        mysql.start();
        Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @AfterAll
    static void stopDatabase() {
        if (mysql != null) {
            mysql.stop();
        }
    }

    @Test
    void v5SeedsExactlyThreeActiveJavaProfilesWithValidHashes() throws Exception {
        try (Connection connection = connection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT profile_id, question_id, language, profile_version, profile_hash, status, active_slot, "
                             + "variable_definitions_json, size_plan_json, variant_plan_json, measurement_limits_json, "
                             + "profile_code, generator_key, generator_version "
                             + "FROM complexity_benchmark_profile ORDER BY question_id")) {
            int count = 0;
            while (rs.next()) {
                count++;
                assertEquals("ACTIVE", rs.getString("status"));
                assertEquals(1, rs.getObject("active_slot"));
                assertEquals("JAVA", rs.getString("language"));
                String storedHash = rs.getString("profile_hash");
                assertEquals(64, storedHash.length());
                assertEquals(storedHash, storedHash.toLowerCase());
                assertCanonicalHashMatchesRow(rs);
            }
            assertEquals(3, count);
        }
    }

    @Test
    void enforcesProfileIdUniqueness() throws SQLException {
        try (Connection connection = connection();
             PreparedStatement ps = connection.prepareStatement("""
                     INSERT INTO complexity_benchmark_profile (
                       profile_id, profile_code, question_id, language, profile_version,
                       generator_key, generator_version, status,
                       variable_definitions_json, size_plan_json, variant_plan_json, measurement_limits_json,
                       profile_hash, created_at, updated_at
                     ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), CAST(? AS JSON), CAST(? AS JSON), ?, NOW(6), NOW(6))
                     """)) {
            ps.setString(1, "cbp-q1-java-v1");
            ps.setString(2, "DUPLICATE");
            ps.setLong(3, 1L);
            ps.setString(4, "JAVA");
            ps.setString(5, "v2");
            ps.setString(6, "INT_MATRIX_ROWS_COLS");
            ps.setString(7, "v1");
            ps.setString(8, "ACTIVE");
            ps.setString(9, "[]");
            ps.setString(10, "{\"ladder\":{\"n\":[8]},\"maxSizes\":{\"n\":8}}");
            ps.setString(11, "{\"variants\":[\"RANDOM\"]}");
            ps.setString(12, "{\"warmups\":1,\"measuredRepeats\":1,\"perInvocationTimeoutMs\":1,\"maxTotalProfileMs\":1}");
            ps.setString(13, "0".repeat(64));
            assertThrows(SQLException.class, ps::executeUpdate);
        }
    }

    @Test
    void seededProfilesHaveExactlyOneActiveSlotPerQuestionLanguage() throws SQLException {
        try (Connection connection = connection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                     SELECT question_id, language, COUNT(*) AS active_count
                     FROM complexity_benchmark_profile
                     WHERE active_slot = 1
                     GROUP BY question_id, language
                     """)) {
            int groups = 0;
            while (rs.next()) {
                groups++;
                assertEquals(1, rs.getInt("active_count"));
            }
            assertEquals(3, groups);
        }
    }

    @Test
    void insertWithActiveStatusDerivesActiveSlotOne() throws SQLException {
        try (Connection connection = connection()) {
            insertProfile(connection, "cbp-q99-java-insert-active", "FOUR_SUM_INT_ARRAY", 99L, "v-insert", "ACTIVE");
            assertEquals(1, readActiveSlot(connection, "cbp-q99-java-insert-active"));
        }
    }

    @Test
    void updateStatusToDisabledClearsDerivedActiveSlot() throws SQLException {
        try (Connection connection = connection()) {
            insertProfile(connection, "cbp-q99-java-toggle", "FOUR_SUM_INT_ARRAY", 99L, "v-toggle", "ACTIVE");
            assertEquals(1, readActiveSlot(connection, "cbp-q99-java-toggle"));
            setStatus(connection, "cbp-q99-java-toggle", "DISABLED");
            assertNull(readActiveSlot(connection, "cbp-q99-java-toggle"));
        }
    }

    @Test
    void updateDisabledProfileToActiveDerivesActiveSlotOne() throws SQLException {
        try (Connection connection = connection()) {
            insertProfile(connection, "cbp-q99-java-reactivate", "FOUR_SUM_INT_ARRAY", 99L, "v-reactivate", "DISABLED");
            assertNull(readActiveSlot(connection, "cbp-q99-java-reactivate"));
            setStatus(connection, "cbp-q99-java-reactivate", "ACTIVE");
            assertEquals(1, readActiveSlot(connection, "cbp-q99-java-reactivate"));
        }
    }

    @Test
    void rejectsSecondActiveProfileForSameQuestionAndLanguage() throws SQLException {
        try (Connection connection = connection()) {
            assertThrows(SQLException.class, () -> insertProfile(
                    connection,
                    "cbp-q10-java-v2",
                    "FOUR_SUM_INT_ARRAY",
                    10L,
                    "v2",
                    "ACTIVE"));
        }
    }

    @Test
    void allowsMultipleDisabledHistoricalVersions() throws SQLException {
        try (Connection connection = connection()) {
            insertProfile(connection, "cbp-q10-java-hist-a", "FOUR_SUM_INT_ARRAY", 10L, "v0-hist-a", "DISABLED");
            insertProfile(connection, "cbp-q10-java-hist-b", "FOUR_SUM_INT_ARRAY", 10L, "v0-hist-b", "DISABLED");
            try (PreparedStatement ps = connection.prepareStatement("""
                    SELECT COUNT(*) FROM complexity_benchmark_profile
                    WHERE question_id = ? AND language = 'JAVA' AND active_slot IS NULL AND status = 'DISABLED'
                    """)) {
                ps.setLong(1, 10L);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertTrue(rs.getInt(1) >= 2);
                }
            }
        }
    }

    @Test
    void disablingOldVersionThenActivatingNewVersionSucceeds() throws SQLException {
        try (Connection connection = connection()) {
            setStatus(connection, "cbp-q10-java-v1", "DISABLED");
            insertProfile(connection, "cbp-q10-java-v2", "FOUR_SUM_INT_ARRAY", 10L, "v2", "ACTIVE");
            try (PreparedStatement ps = connection.prepareStatement("""
                    SELECT profile_version FROM complexity_benchmark_profile
                    WHERE question_id = ? AND language = 'JAVA' AND active_slot = 1
                    """)) {
                ps.setLong(1, 10L);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("v2", rs.getString(1));
                    assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    void activeLookupUsesActiveSlotNotLexicalProfileVersion() throws SQLException {
        try (Connection connection = connection()) {
            insertProfile(connection, "cbp-q10-java-v9-disabled", "FOUR_SUM_INT_ARRAY", 10L, "v9", "DISABLED");
            try (PreparedStatement ps = connection.prepareStatement("""
                    SELECT profile_version FROM complexity_benchmark_profile
                    WHERE question_id = ? AND language = 'JAVA' AND active_slot = 1
                    """)) {
                ps.setLong(1, 10L);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("v1", rs.getString(1));
                    assertFalse(rs.next());
                }
            }
        }
    }

    private static void insertProfile(
            Connection connection,
            String profileId,
            String profileCode,
            long questionId,
            String profileVersion,
            String status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO complexity_benchmark_profile (
                  profile_id, profile_code, question_id, language, profile_version,
                  generator_key, generator_version, status,
                  variable_definitions_json, size_plan_json, variant_plan_json, measurement_limits_json,
                  profile_hash, created_at, updated_at
                ) VALUES (?, ?, ?, 'JAVA', ?, 'INT_ARRAY_WITH_TARGET', 'v1', ?,
                  CAST(? AS JSON), CAST(? AS JSON), CAST(? AS JSON), CAST(? AS JSON),
                  ?, NOW(6), NOW(6))
                """)) {
            ps.setString(1, profileId);
            ps.setString(2, profileCode);
            ps.setLong(3, questionId);
            ps.setString(4, profileVersion);
            ps.setString(5, status);
            ps.setString(6, "[{\"name\":\"n\",\"meaning\":\"length\",\"parameter\":\"nums\",\"dimension\":\"length\"}]");
            ps.setString(7, "{\"ladder\":{\"n\":[64]},\"maxSizes\":{\"n\":512}}");
            ps.setString(8, "{\"variants\":[\"RANDOM\"]}");
            ps.setString(9, "{\"warmups\":3,\"measuredRepeats\":5,\"perInvocationTimeoutMs\":1000,\"maxTotalProfileMs\":15000}");
            ps.setString(10, "0".repeat(64));
            ps.executeUpdate();
        }
    }

    private static void setStatus(Connection connection, String profileId, String status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                UPDATE complexity_benchmark_profile SET status = ? WHERE profile_id = ?
                """)) {
            ps.setString(1, status);
            ps.setString(2, profileId);
            assertEquals(1, ps.executeUpdate());
        }
    }

    private static Integer readActiveSlot(Connection connection, String profileId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT active_slot FROM complexity_benchmark_profile WHERE profile_id = ?
                """)) {
            ps.setString(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return (Integer) rs.getObject("active_slot");
            }
        }
    }

    private static void assertCanonicalHashMatchesRow(ResultSet rs) throws Exception {
        List<VariableDefinition> variables = MAPPER.readValue(
                rs.getString("variable_definitions_json"),
                MAPPER.getTypeFactory().constructCollectionType(List.class, VariableDefinition.class));
        var sizePlan = MAPPER.readTree(rs.getString("size_plan_json"));
        Map<String, List<Integer>> ladder = parseLadder(sizePlan);
        Map<String, Integer> maxSizes = parseMaxSizes(sizePlan);
        List<String> variants = parseVariants(MAPPER.readTree(rs.getString("variant_plan_json")));
        var limitsNode = MAPPER.readTree(rs.getString("measurement_limits_json"));
        MeasurementLimits limits = new MeasurementLimits(
                limitsNode.path("warmups").asInt(),
                limitsNode.path("measuredRepeats").asInt(),
                limitsNode.path("perInvocationTimeoutMs").asInt(),
                limitsNode.path("maxTotalProfileMs").asInt());
        String expected = ProfileCanonicalJson.profileHash(
                rs.getLong("question_id"),
                rs.getString("language"),
                rs.getString("profile_code"),
                rs.getString("profile_version"),
                rs.getString("generator_key"),
                rs.getString("generator_version"),
                variables,
                ladder,
                maxSizes,
                variants,
                limits);
        assertEquals(expected, rs.getString("profile_hash"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<Integer>> parseLadder(com.fasterxml.jackson.databind.JsonNode sizePlan) {
        Map<String, List<Integer>> ladder = new java.util.LinkedHashMap<>();
        sizePlan.get("ladder").fields().forEachRemaining(entry -> {
            List<Integer> values = new java.util.ArrayList<>();
            entry.getValue().forEach(v -> values.add(v.asInt()));
            ladder.put(entry.getKey(), values);
        });
        return ladder;
    }

    private static Map<String, Integer> parseMaxSizes(com.fasterxml.jackson.databind.JsonNode sizePlan) {
        Map<String, Integer> max = new java.util.LinkedHashMap<>();
        sizePlan.get("maxSizes").fields().forEachRemaining(entry -> max.put(entry.getKey(), entry.getValue().asInt()));
        return max;
    }

    private static List<String> parseVariants(com.fasterxml.jackson.databind.JsonNode variantPlan) {
        List<String> variants = new java.util.ArrayList<>();
        variantPlan.get("variants").forEach(v -> variants.add(v.asText()));
        return variants;
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
    }
}
