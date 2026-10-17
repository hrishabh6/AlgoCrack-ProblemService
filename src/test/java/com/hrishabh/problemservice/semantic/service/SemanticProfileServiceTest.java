package com.hrishabh.problemservice.semantic.service;

import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import com.hrishabh.problemservice.complexity.repository.ComplexityBenchmarkProfileRepository;
import com.hrishabh.problemservice.models.Language;
import com.hrishabh.problemservice.models.QuestionMetadata;
import com.hrishabh.problemservice.repository.QuestionMetadataRepository;
import com.hrishabh.problemservice.semantic.catalog.SemanticProfileCatalog;
import com.hrishabh.problemservice.semantic.model.SemanticCompatibility;
import com.hrishabh.problemservice.semantic.model.SemanticProblemProfile;
import com.hrishabh.problemservice.semantic.model.SemanticProfileStatus;
import com.hrishabh.problemservice.semantic.repository.SemanticProblemProfileRepository;
import com.hrishabh.problemservice.semantic.support.SemanticProfileCodec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SemanticProfileServiceTest {

    private final SemanticProblemProfileRepository profiles = mock(SemanticProblemProfileRepository.class);
    private final QuestionMetadataRepository metadata = mock(QuestionMetadataRepository.class);
    private final ComplexityBenchmarkProfileRepository benchmarks = mock(ComplexityBenchmarkProfileRepository.class);
    private final SemanticProfileService service = new SemanticProfileService(profiles, metadata, benchmarks);

    @Test
    void missingProfileIsNotReportedAsIncompatible() {
        when(profiles.findByQuestionIdAndStatus(99L, SemanticProfileStatus.ACTIVE)).thenReturn(Optional.empty());

        SemanticProfileService.SemanticProfileRead read = service.read(99L, "JAVA");

        assertEquals(404, read.httpStatus());
        assertEquals(SemanticProfileService.MISSING, read.code());
        assertNull(read.compatibility());
        assertNull(read.profileId());
    }

    @Test
    void staleHashIsDistinctFromAMissingProfile() {
        SemanticProblemProfile stored = active(10, "not-the-canonical-hash");
        when(profiles.findByQuestionIdAndStatus(10L, SemanticProfileStatus.ACTIVE)).thenReturn(Optional.of(stored));

        SemanticProfileService.SemanticProfileRead read = service.read(10L, "JAVA");

        assertEquals(409, read.httpStatus());
        assertEquals(SemanticProfileService.STALE, read.code());
        assertEquals(SemanticCompatibility.STALE, read.compatibility());
        assertEquals("spp-q10-v1", read.profileId());
        assertNull(read.profile());
    }

    @Test
    void javaReadReturnsTheActiveCompatibleProfile() {
        SemanticProblemProfile stored = active(10, SemanticProfileCodec.profileHash(SemanticProfileCatalog.seedFor(10).document()));
        when(profiles.findByQuestionIdAndStatus(10L, SemanticProfileStatus.ACTIVE)).thenReturn(Optional.of(stored));
        when(metadata.findByQuestionIdAndLanguage(10L, Language.JAVA)).thenReturn(Optional.of(fourSum()));
        when(benchmarks.findByQuestionIdAndLanguageAndActiveSlot(10L, "JAVA", 1)).thenReturn(Optional.of(fourSumBenchmark()));

        SemanticProfileService.SemanticProfileRead read = service.read(10L, "JAVA");

        assertEquals(200, read.httpStatus());
        assertEquals(SemanticCompatibility.COMPATIBLE, read.compatibility());
        assertEquals("n", read.profile().sizeVariables().get(0).name());
    }

    @Test
    void pythonIncompatibilityDoesNotChangeTheStoredActiveProfile() {
        SemanticProblemProfile stored = active(2, SemanticProfileCodec.profileHash(SemanticProfileCatalog.seedFor(2).document()));
        when(profiles.findByQuestionIdAndStatus(2L, SemanticProfileStatus.ACTIVE)).thenReturn(Optional.of(stored));
        when(metadata.findByQuestionIdAndLanguage(2L, Language.PYTHON)).thenReturn(Optional.of(
                QuestionMetadata.builder()
                        .functionName("reverseKGroup")
                        .returnType("ListNode")
                        .paramTypes(List.of("List", "int"))
                        .paramNames(List.of("head", "k"))
                        .language(Language.PYTHON)
                        .build()));

        SemanticProfileService.SemanticProfileRead python = service.read(2L, "PYTHON");

        assertEquals(409, python.httpStatus());
        assertEquals(SemanticProfileService.INCOMPATIBLE, python.code());
        assertEquals(SemanticProfileStatus.ACTIVE, stored.getStatus());
    }

    private static SemanticProblemProfile active(long questionId, String hash) {
        var document = SemanticProfileCatalog.seedFor(questionId).document();
        return SemanticProblemProfile.builder()
                .profileId("spp-q" + questionId + "-v1")
                .questionId(questionId)
                .semanticSchemaVersion("semantic-v1")
                .semanticVersion("v1")
                .status(SemanticProfileStatus.ACTIVE)
                .profileJson(SemanticProfileCodec.canonicalJson(document))
                .profileHash(hash)
                .build();
    }

    private static QuestionMetadata fourSum() {
        return QuestionMetadata.builder()
                .functionName("fourSum")
                .returnType("List<List<Integer>>")
                .paramTypes(List.of("int[]", "int"))
                .paramNames(List.of("nums", "target"))
                .language(Language.JAVA)
                .build();
    }

    private static ComplexityBenchmarkProfile fourSumBenchmark() {
        return ComplexityBenchmarkProfile.builder()
                .variableDefinitionsJson("""
                        [{"name":"n","meaning":"length of nums","parameter":"nums","dimension":"length"}]
                        """)
                .build();
    }
}
