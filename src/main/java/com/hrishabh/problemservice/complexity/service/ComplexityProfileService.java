package com.hrishabh.problemservice.complexity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.*;
import com.hrishabh.problemservice.complexity.generator.BenchmarkGenerator;
import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import com.hrishabh.problemservice.complexity.model.ComplexityProfileStatus;
import com.hrishabh.problemservice.complexity.repository.ComplexityBenchmarkProfileRepository;
import com.hrishabh.problemservice.complexity.support.ComplexityProfileLimits;
import com.hrishabh.problemservice.complexity.support.DeterministicSeedDeriver;
import com.hrishabh.problemservice.complexity.support.ProfileContentHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ComplexityProfileService {

    private final ComplexityBenchmarkProfileRepository repository;
    private final ComplexityProfileValidator validator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<ProfileMetadataResponse> getActiveProfile(long questionId, String language) {
        return repository.findFirstByQuestionIdAndLanguageAndStatusOrderByProfileVersionDesc(
                        questionId, language.toUpperCase(Locale.ROOT), ComplexityProfileStatus.ACTIVE)
                .map(entity -> toMetadata(validator.validateAndParse(entity)));
    }

    @Transactional(readOnly = true)
    public CasesResponse generateCases(long questionId, CasesRequest request) {
        String language = request.language().toUpperCase(Locale.ROOT);
        ProfileMetadataResponse active = getActiveProfile(questionId, language)
                .orElseThrow(() -> new ProfileUnavailableException("no active profile"));
        if (!active.profileVersion().equals(request.profileVersion())) {
            throw new ProfileUnavailableException("requested profile version is not active");
        }
        if (!active.profileHash().equals(request.profileHash())) {
            throw new ProfileUnavailableException("profile hash mismatch");
        }

        ComplexityBenchmarkProfile entity = repository
                .findByQuestionIdAndLanguageAndProfileVersion(questionId, language, request.profileVersion())
                .orElseThrow(() -> new ProfileUnavailableException("profile version not found"));
        ComplexityProfileValidator.ParsedProfile parsed = validator.validateAndParse(entity);

        List<GeneratedCaseDto> cases = new ArrayList<>();
        List<Map<String, Integer>> sizePoints = enumerateSizePoints(parsed.ladder());
        for (Map<String, Integer> sizeVector : sizePoints) {
            for (String variant : parsed.variants()) {
                if (cases.size() >= ComplexityProfileLimits.MAX_CASES_PER_REQUEST) {
                    break;
                }
                cases.add(generateCase(questionId, parsed, sizeVector, variant));
            }
        }
        return new CasesResponse(
                entity.getQuestionId(),
                entity.getLanguage(),
                entity.getProfileId(),
                entity.getProfileCode(),
                entity.getProfileVersion(),
                ComplexityProfileValidator.expectedProfileHash(
                        entity, parsed.variables(), parsed.ladder(), parsed.maxSizes(), parsed.variants(), parsed.limits()),
                entity.getGeneratorKey(),
                entity.getGeneratorVersion(),
                cases);
    }

    private GeneratedCaseDto generateCase(
            long questionId,
            ComplexityProfileValidator.ParsedProfile parsed,
            Map<String, Integer> sizeVector,
            String variant) {
        String seedMaterial = DeterministicSeedDeriver.seedMaterial(
                questionId,
                parsed.entity().getProfileVersion(),
                parsed.entity().getGeneratorVersion(),
                sizeVector,
                variant);
        long seedLong = DeterministicSeedDeriver.seedLong(seedMaterial);
        String seed = DeterministicSeedDeriver.seedValue(seedMaterial);
        BenchmarkGenerator generator = parsed.generator();
        JsonNode input = generator.generateInput(sizeVector, variant, seedLong);
        enforceSerializedBounds(input);
        String serializedInput;
        try {
            serializedInput = objectMapper.writeValueAsString(input);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new InvalidComplexityProfileException("failed to serialize generated input");
        }
        String inputHash = ProfileContentHasher.sha256Hex(serializedInput);
        String caseIdentity = ProfileContentHasher.sha256Hex(seedMaterial);
        String caseId = caseIdentity.substring(0, 16) + "-" + variant.toLowerCase(Locale.ROOT);
        return new GeneratedCaseDto(
                parsed.entity().getProfileCode(),
                parsed.entity().getProfileVersion(),
                ComplexityProfileValidator.expectedProfileHash(
                        parsed.entity(), parsed.variables(), parsed.ladder(), parsed.maxSizes(), parsed.variants(), parsed.limits()),
                parsed.entity().getGeneratorVersion(),
                caseId,
                caseIdentity,
                sizeVector,
                variant,
                seed,
                serializedInput,
                input,
                inputHash);
    }

    private void enforceSerializedBounds(JsonNode input) {
        String json = input.toString();
        if (json.length() > ComplexityProfileLimits.MAX_SERIALIZED_INPUT_CHARS) {
            throw new InvalidComplexityProfileException("generated input too large");
        }
        int elements = countNumericElements(input);
        if (elements > ComplexityProfileLimits.MAX_GENERATED_ELEMENTS) {
            throw new InvalidComplexityProfileException("generated input element count too large");
        }
    }

    private static int countNumericElements(JsonNode node) {
        if (node.isNumber()) {
            return 1;
        }
        int count = 0;
        if (node.isArray()) {
            for (JsonNode child : node) {
                count += countNumericElements(child);
            }
        }
        return count;
    }

    private static List<Map<String, Integer>> enumerateSizePoints(Map<String, List<Integer>> ladder) {
        List<String> keys = new ArrayList<>(ladder.keySet());
        List<Map<String, Integer>> points = new ArrayList<>();
        enumerateRecursive(ladder, keys, 0, new LinkedHashMap<>(), points);
        return points;
    }

    private static void enumerateRecursive(
            Map<String, List<Integer>> ladder,
            List<String> keys,
            int index,
            Map<String, Integer> current,
            List<Map<String, Integer>> out) {
        if (index >= keys.size()) {
            out.add(Map.copyOf(current));
            return;
        }
        String key = keys.get(index);
        for (Integer value : ladder.get(key)) {
            current.put(key, value);
            enumerateRecursive(ladder, keys, index + 1, current, out);
        }
    }

    private static String buildCaseId(Map<String, Integer> sizeVector, String variant) {
        StringBuilder sb = new StringBuilder();
        new TreeMap<>(sizeVector).forEach((k, v) -> sb.append(k).append(v));
        sb.append("-").append(variant.toLowerCase(Locale.ROOT));
        return sb.toString();
    }

    private ProfileMetadataResponse toMetadata(ComplexityProfileValidator.ParsedProfile parsed) {
        ComplexityBenchmarkProfile entity = parsed.entity();
        return new ProfileMetadataResponse(
                entity.getQuestionId(),
                entity.getLanguage(),
                entity.getProfileId(),
                entity.getProfileCode(),
                entity.getProfileVersion(),
                ComplexityProfileValidator.expectedProfileHash(
                        entity, parsed.variables(), parsed.ladder(), parsed.maxSizes(), parsed.variants(), parsed.limits()),
                entity.getGeneratorKey(),
                entity.getGeneratorVersion(),
                parsed.variables(),
                parsed.ladder(),
                parsed.maxSizes(),
                parsed.variants(),
                parsed.limits().warmups(),
                parsed.limits().measuredRepeats(),
                parsed.limits().perInvocationTimeoutMs(),
                parsed.limits().maxTotalProfileMs());
    }

}
