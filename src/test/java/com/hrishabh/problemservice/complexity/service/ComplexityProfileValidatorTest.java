package com.hrishabh.problemservice.complexity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.problemservice.complexity.generator.BenchmarkGeneratorRegistry;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.VariableDefinition;
import com.hrishabh.problemservice.complexity.generator.IntArrayWithTargetGenerator;
import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import com.hrishabh.problemservice.complexity.model.ComplexityProfileStatus;
import com.hrishabh.problemservice.complexity.support.ProfileCanonicalJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComplexityProfileValidatorTest {

    private ComplexityProfileValidator validator;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        BenchmarkGeneratorRegistry registry = new BenchmarkGeneratorRegistry(List.of(new IntArrayWithTargetGenerator(mapper)));
        validator = new ComplexityProfileValidator(mapper, registry);
    }

    @Test
    void rejectsUnknownGenerator() {
        ComplexityBenchmarkProfile profile = baseProfile();
        profile.setGeneratorKey("NOT_A_GENERATOR");
        assertThrows(InvalidComplexityProfileException.class, () -> validator.validateAndParse(profile));
    }

    @Test
    void rejectsNegativeLadderValues() {
        ComplexityBenchmarkProfile profile = baseProfile();
        profile.setSizePlanJson("""
                {"ladder":{"n":[-1]},"maxSizes":{"n":512}}
                """);
        assertThrows(InvalidComplexityProfileException.class, () -> validator.validateAndParse(profile));
    }

    @Test
    void rejectsIncompatibleVariant() {
        ComplexityBenchmarkProfile profile = baseProfile();
        profile.setVariantPlanJson("""
                {"variants":["NOT_SUPPORTED"]}
                """);
        assertThrows(InvalidComplexityProfileException.class, () -> validator.validateAndParse(profile));
    }

    @Test
    void acceptsProfileWhenCanonicalHashMatches() {
        ComplexityBenchmarkProfile profile = baseProfile();
        assertDoesNotThrow(() -> validator.validateAndParse(profile));
    }

    @Test
    void rejectsProfileHashMismatch() {
        ComplexityBenchmarkProfile profile = baseProfile();
        profile.setProfileHash("0".repeat(64));
        assertThrows(InvalidComplexityProfileException.class, () -> validator.validateAndParse(profile));
    }

    private static ComplexityBenchmarkProfile baseProfile() {
        String hash = ProfileCanonicalJson.profileHash(
                10L,
                "JAVA",
                "FOUR_SUM_INT_ARRAY",
                "v1",
                "INT_ARRAY_WITH_TARGET",
                "v1",
                List.of(new VariableDefinition("n", "length of nums", "nums", "length")),
                Map.of("n", List.of(64, 128)),
                Map.of("n", 512),
                List.of("RANDOM", "SORTED"),
                new ComplexityProfileValidator.MeasurementLimits(3, 5, 1000, 15000));
        return ComplexityBenchmarkProfile.builder()
                .profileId("test-profile")
                .profileCode("FOUR_SUM_INT_ARRAY")
                .questionId(10L)
                .language("JAVA")
                .profileVersion("v1")
                .generatorKey("INT_ARRAY_WITH_TARGET")
                .generatorVersion("v1")
                .status(ComplexityProfileStatus.ACTIVE)
                .variableDefinitionsJson("""
                        [{"name":"n","meaning":"length of nums","parameter":"nums","dimension":"length"}]
                        """)
                .sizePlanJson("""
                        {"ladder":{"n":[64,128]},"maxSizes":{"n":512}}
                        """)
                .variantPlanJson("""
                        {"variants":["RANDOM","SORTED"]}
                        """)
                .measurementLimitsJson("""
                        {"warmups":3,"measuredRepeats":5,"perInvocationTimeoutMs":1000,"maxTotalProfileMs":15000}
                        """)
                .profileHash(hash)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
