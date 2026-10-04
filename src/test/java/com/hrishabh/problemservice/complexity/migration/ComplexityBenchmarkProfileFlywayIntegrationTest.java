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
                     "SELECT profile_id, question_id, language, profile_version, profile_hash, status, "
                             + "variable_definitions_json, size_plan_json, variant_plan_json, measurement_limits_json, "
                             + "profile_code, generator_key, generator_version "
                             + "FROM complexity_benchmark_profile ORDER BY question_id")) {
            int count = 0;
            while (rs.next()) {
                count++;
                assertEquals("ACTIVE", rs.getString("status"));
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
    void activeProfileSelectionIsDeterministicByVersion() throws SQLException {
        try (Connection connection = connection();
             PreparedStatement ps = connection.prepareStatement("""
                     SELECT profile_version FROM complexity_benchmark_profile
                     WHERE question_id = ? AND language = ? AND status = 'ACTIVE'
                     ORDER BY profile_version DESC LIMIT 1
                     """)) {
            ps.setLong(1, 10L);
            ps.setString(2, "JAVA");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("v1", rs.getString(1));
                assertFalse(rs.next());
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
