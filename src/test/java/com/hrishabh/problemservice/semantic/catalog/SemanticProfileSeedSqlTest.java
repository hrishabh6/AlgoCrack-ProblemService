package com.hrishabh.problemservice.semantic.catalog;

import com.hrishabh.problemservice.semantic.support.SemanticProfileCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticProfileSeedSqlTest {

    @Test
    void migrationBackfillsEveryApprovedProfileHash() throws Exception {
        String sql = new String(
                getClass().getResourceAsStream("/db/migration/V8__semantic_problem_profile_seed.sql").readAllBytes(),
                StandardCharsets.UTF_8);
        String schema = new String(
                getClass().getResourceAsStream("/db/migration/V7__semantic_problem_profile.sql").readAllBytes(),
                StandardCharsets.UTF_8);
        assertTrue(schema.contains("uk_semantic_question_active_slot"));
        assertTrue(schema.contains("fk_semantic_profile_question"));
        for (SemanticProfileCatalog.Seed seed : SemanticProfileCatalog.seeds()) {
            assertTrue(sql.contains(seed.profileId()));
            assertTrue(sql.contains(SemanticProfileCodec.profileHash(seed.document())));
            assertTrue(sql.contains("'ACTIVE'"));
        }
    }
}
