package com.hrishabh.problemservice.complexity.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.VariableDefinition;
import com.hrishabh.problemservice.complexity.service.ComplexityProfileValidator.MeasurementLimits;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Deterministic semantic canonical form for {@code profile_hash}.
 * Excludes DB ids, timestamps, status, and profile_id.
 */
public final class ProfileCanonicalJson {

    private static final ObjectMapper CANONICAL_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private ProfileCanonicalJson() {
    }

    public static String canonicalPayload(
            long questionId,
            String language,
            String profileCode,
            String profileVersion,
            String generatorKey,
            String generatorVersion,
            List<VariableDefinition> variables,
            Map<String, List<Integer>> ladder,
            Map<String, Integer> maxSizes,
            List<String> variants,
            MeasurementLimits limits) {
        try {
            ObjectNode root = CANONICAL_MAPPER.createObjectNode();
            root.put("questionId", questionId);
            root.put("language", language.toUpperCase());
            root.put("profileCode", profileCode);
            root.put("profileVersion", profileVersion);
            root.put("generatorKey", generatorKey);
            root.put("generatorVersion", generatorVersion);
            root.set("variables", variablesNode(variables));
            ObjectNode sizePlan = CANONICAL_MAPPER.createObjectNode();
            ObjectNode ladderNode = CANONICAL_MAPPER.createObjectNode();
            new TreeMap<>(ladder).forEach((key, values) -> {
                ArrayNode arr = CANONICAL_MAPPER.createArrayNode();
                values.forEach(arr::add);
                ladderNode.set(key, arr);
            });
            sizePlan.set("ladder", ladderNode);
            ObjectNode maxNode = CANONICAL_MAPPER.createObjectNode();
            new TreeMap<>(maxSizes).forEach(maxNode::put);
            sizePlan.set("maxSizes", maxNode);
            root.set("sizePlan", sizePlan);
            ArrayNode variantsNode = CANONICAL_MAPPER.createArrayNode();
            variants.forEach(variantsNode::add);
            root.set("variants", variantsNode);
            ObjectNode limitsNode = CANONICAL_MAPPER.createObjectNode();
            limitsNode.put("warmups", limits.warmups());
            limitsNode.put("measuredRepeats", limits.measuredRepeats());
            limitsNode.put("perInvocationTimeoutMs", limits.perInvocationTimeoutMs());
            limitsNode.put("maxTotalProfileMs", limits.maxTotalProfileMs());
            root.set("measurementLimits", limitsNode);
            return CANONICAL_MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to build canonical profile json", e);
        }
    }

    public static String profileHash(
            long questionId,
            String language,
            String profileCode,
            String profileVersion,
            String generatorKey,
            String generatorVersion,
            List<VariableDefinition> variables,
            Map<String, List<Integer>> ladder,
            Map<String, Integer> maxSizes,
            List<String> variants,
            MeasurementLimits limits) {
        return ProfileContentHasher.sha256Hex(canonicalPayload(
                questionId, language, profileCode, profileVersion, generatorKey, generatorVersion,
                variables, ladder, maxSizes, variants, limits));
    }

    private static ArrayNode variablesNode(List<VariableDefinition> variables) {
        List<VariableDefinition> sorted = new ArrayList<>(variables);
        sorted.sort(Comparator.comparing(VariableDefinition::name));
        ArrayNode array = CANONICAL_MAPPER.createArrayNode();
        for (VariableDefinition variable : sorted) {
            ObjectNode node = CANONICAL_MAPPER.createObjectNode();
            node.put("name", variable.name());
            node.put("meaning", variable.meaning());
            node.put("parameter", variable.parameter());
            if (variable.dimension() != null) {
                node.put("dimension", variable.dimension());
            }
            array.add(node);
        }
        return array;
    }
}
