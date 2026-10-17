package com.hrishabh.problemservice.semantic.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hrishabh.problemservice.complexity.repository.ComplexityBenchmarkProfileRepository;
import com.hrishabh.problemservice.models.Language;
import com.hrishabh.problemservice.models.QuestionMetadata;
import com.hrishabh.problemservice.repository.QuestionMetadataRepository;
import com.hrishabh.problemservice.semantic.dto.SemanticProfileDocument;
import com.hrishabh.problemservice.semantic.model.SemanticCompatibility;
import com.hrishabh.problemservice.semantic.model.SemanticProblemProfile;
import com.hrishabh.problemservice.semantic.model.SemanticProfileStatus;
import com.hrishabh.problemservice.semantic.repository.SemanticProblemProfileRepository;
import com.hrishabh.problemservice.semantic.service.SemanticProfileValidator.BenchmarkVariable;
import com.hrishabh.problemservice.semantic.support.SemanticProfileCodec;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SemanticProfileService {

    public static final String MISSING = "SEMANTIC_PROFILE_MISSING";
    public static final String STALE = "SEMANTIC_PROFILE_STALE";
    public static final String INCOMPATIBLE = "SEMANTIC_PROFILE_INCOMPATIBLE";
    public static final String ACTIVE = "SEMANTIC_PROFILE_ACTIVE";

    private final SemanticProblemProfileRepository profiles;
    private final QuestionMetadataRepository metadataRepository;
    private final ComplexityBenchmarkProfileRepository benchmarkProfiles;

    public SemanticProfileService(
            SemanticProblemProfileRepository profiles,
            QuestionMetadataRepository metadataRepository,
            ComplexityBenchmarkProfileRepository benchmarkProfiles) {
        this.profiles = profiles;
        this.metadataRepository = metadataRepository;
        this.benchmarkProfiles = benchmarkProfiles;
    }

    public SemanticProfileRead read(long questionId, String languageName) {
        SemanticProblemProfile stored = profiles
                .findByQuestionIdAndStatus(questionId, SemanticProfileStatus.ACTIVE)
                .orElse(null);
        if (stored == null || stored.getStatus() != SemanticProfileStatus.ACTIVE) {
            return SemanticProfileRead.missing();
        }
        Language language = parseLanguage(languageName);
        if (language == null) {
            return SemanticProfileRead.incompatible(
                    stored, SemanticCompatibility.INCOMPATIBLE, List.of("unsupported language: " + languageName));
        }
        SemanticProfileDocument document;
        try {
            document = SemanticProfileCodec.parse(stored.getProfileJson());
        } catch (JsonProcessingException ex) {
            return SemanticProfileRead.stale(stored, List.of("semantic profile JSON is not a V1 document"));
        }
        if (!SemanticProfileCodec.profileHash(document).equals(stored.getProfileHash())) {
            return SemanticProfileRead.stale(stored, List.of("profile hash does not match canonical document"));
        }
        List<String> documentFailures = SemanticProfileValidator.validateDocument(document);
        if (!documentFailures.isEmpty()) {
            return SemanticProfileRead.stale(stored, documentFailures);
        }
        QuestionMetadata metadata = metadataRepository.findByQuestionIdAndLanguage(questionId, language).orElse(null);
        if (metadata == null) {
            if (language == Language.JAVA) {
                return SemanticProfileRead.incompatible(
                        stored, SemanticCompatibility.INCOMPATIBLE, List.of("Java metadata is missing"));
            }
            return SemanticProfileRead.ready(stored, document, SemanticCompatibility.UNCHECKED, List.of());
        }
        List<String> failures = new ArrayList<>(SemanticProfileValidator.validateLanguage(document, metadata));
        if (language == Language.JAVA) {
            failures.addAll(SemanticProfileValidator.validateBenchmarkAgreement(
                    document,
                    metadata.getParamNames(),
                    benchmarkVariables(questionId)));
        }
        if (!failures.isEmpty()) {
            return SemanticProfileRead.incompatible(stored, SemanticCompatibility.INCOMPATIBLE, failures);
        }
        return SemanticProfileRead.ready(stored, document, SemanticCompatibility.COMPATIBLE, List.of());
    }

    private List<BenchmarkVariable> benchmarkVariables(long questionId) {
        return benchmarkProfiles
                .findByQuestionIdAndLanguageAndActiveSlot(questionId, Language.JAVA.name(), 1)
                .map(SemanticBenchmarkAgreement::variables)
                .orElse(List.of());
    }

    private static Language parseLanguage(String languageName) {
        if (languageName == null || languageName.isBlank()) {
            return null;
        }
        try {
            return Language.valueOf(languageName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SemanticProfileRead(
            int httpStatus,
            String code,
            String profileId,
            String schemaVersion,
            String semanticVersion,
            String status,
            String profileHash,
            SemanticCompatibility compatibility,
            List<String> reasons,
            SemanticProfileDocument profile) {

        static SemanticProfileRead missing() {
            return new SemanticProfileRead(404, MISSING, null, null, null, null, null, null, List.of(), null);
        }

        static SemanticProfileRead stale(SemanticProblemProfile stored, List<String> reasons) {
            return new SemanticProfileRead(
                    409, STALE, stored.getProfileId(), stored.getSemanticSchemaVersion(), stored.getSemanticVersion(),
                    stored.getStatus().name(), stored.getProfileHash(), SemanticCompatibility.STALE, reasons, null);
        }

        static SemanticProfileRead incompatible(
                SemanticProblemProfile stored,
                SemanticCompatibility compatibility,
                List<String> reasons) {
            return new SemanticProfileRead(
                    409, INCOMPATIBLE, stored.getProfileId(), stored.getSemanticSchemaVersion(),
                    stored.getSemanticVersion(), stored.getStatus().name(), stored.getProfileHash(),
                    compatibility, reasons, null);
        }

        static SemanticProfileRead ready(
                SemanticProblemProfile stored,
                SemanticProfileDocument document,
                SemanticCompatibility compatibility,
                List<String> reasons) {
            return new SemanticProfileRead(
                    200, ACTIVE, stored.getProfileId(), stored.getSemanticSchemaVersion(), stored.getSemanticVersion(),
                    stored.getStatus().name(), stored.getProfileHash(), compatibility, reasons, document);
        }
    }
}
