package com.hrishabh.problemservice.complexity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.problemservice.complexity.generator.BenchmarkGeneratorRegistry;
import com.hrishabh.problemservice.complexity.generator.IntArrayWithTargetGenerator;
import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import com.hrishabh.problemservice.complexity.model.ComplexityProfileStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

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
    void acceptsPlaceholderHashProfiles() {
        ComplexityBenchmarkProfile profile = baseProfile();
        assertDoesNotThrow(() -> validator.validateAndParse(profile));
    }

    private static ComplexityBenchmarkProfile baseProfile() {
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
                .profileHash("sha256:placeholder")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
