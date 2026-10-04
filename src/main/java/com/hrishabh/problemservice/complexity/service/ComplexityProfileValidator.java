package com.hrishabh.problemservice.complexity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.VariableDefinition;
import com.hrishabh.problemservice.complexity.generator.BenchmarkGenerator;
import com.hrishabh.problemservice.complexity.generator.BenchmarkGeneratorRegistry;
import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import com.hrishabh.problemservice.complexity.support.ComplexityProfileLimits;
import com.hrishabh.problemservice.complexity.support.ProfileContentHasher;
import com.hrishabh.problemservice.models.Language;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ComplexityProfileValidator {

    private final ObjectMapper objectMapper;
    private final BenchmarkGeneratorRegistry generatorRegistry;

    public ComplexityProfileValidator(ObjectMapper objectMapper, BenchmarkGeneratorRegistry generatorRegistry) {
        this.objectMapper = objectMapper;
        this.generatorRegistry = generatorRegistry;
    }

    public ParsedProfile validateAndParse(ComplexityBenchmarkProfile profile) {
        try {
            Language.valueOf(profile.getLanguage().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidComplexityProfileException("unsupported language: " + profile.getLanguage());
        }
        BenchmarkGenerator generator = generatorRegistry
                .find(profile.getGeneratorKey(), profile.getGeneratorVersion())
                .orElseThrow(() -> new InvalidComplexityProfileException("unknown generator: "
                        + profile.getGeneratorKey() + "@" + profile.getGeneratorVersion()));

        List<VariableDefinition> variables = parseVariables(profile.getVariableDefinitionsJson());
        if (variables.isEmpty()) {
            throw new InvalidComplexityProfileException("variables required");
        }
        Map<String, List<Integer>> ladder = parseLadder(profile.getSizePlanJson());
        Map<String, Integer> maxSizes = parseMaxSizes(profile.getSizePlanJson());
        List<String> variants = parseVariants(profile.getVariantPlanJson());
        MeasurementLimits limits = parseLimits(profile.getMeasurementLimitsJson());

        validateLadder(ladder, maxSizes, variables);
        validateVariants(variants, generator);

        if (!profile.getProfileHash().startsWith("sha256:")) {
            String canonical = canonicalContent(profile, variables, ladder, maxSizes, variants, limits);
            String expectedHash = ProfileContentHasher.sha256Hex(canonical);
            if (!expectedHash.equals(profile.getProfileHash())) {
                throw new InvalidComplexityProfileException("profile hash mismatch");
            }
        }

        return new ParsedProfile(profile, variables, ladder, maxSizes, variants, limits, generator);
    }

    public static String canonicalContent(
            ComplexityBenchmarkProfile profile,
            List<VariableDefinition> variables,
            Map<String, List<Integer>> ladder,
            Map<String, Integer> maxSizes,
            List<String> variants,
            MeasurementLimits limits) {
        return profile.getQuestionId() + "|"
                + profile.getLanguage() + "|"
                + profile.getProfileVersion() + "|"
                + profile.getGeneratorKey() + "|"
                + profile.getGeneratorVersion() + "|"
                + variables + "|"
                + new TreeMap<>(ladder) + "|"
                + new TreeMap<>(maxSizes) + "|"
                + variants + "|"
                + limits;
    }

    private void validateLadder(
            Map<String, List<Integer>> ladder,
            Map<String, Integer> maxSizes,
            List<VariableDefinition> variables) {
        Set<String> names = new HashSet<>();
        for (VariableDefinition variable : variables) {
            names.add(variable.name());
        }
        for (String name : names) {
            List<Integer> values = ladder.get(name);
            if (values == null || values.isEmpty()) {
                throw new InvalidComplexityProfileException("missing ladder for " + name);
            }
            if (values.size() > ComplexityProfileLimits.MAX_LADDER_VALUES_PER_VARIABLE) {
                throw new InvalidComplexityProfileException("ladder too long for " + name);
            }
            int max = maxSizes.getOrDefault(name, Integer.MAX_VALUE);
            for (Integer value : values) {
                if (value == null || value <= 0) {
                    throw new InvalidComplexityProfileException("non-positive ladder value");
                }
                if (value > max) {
                    throw new InvalidComplexityProfileException("ladder exceeds max for " + name);
                }
            }
        }
    }

    private void validateVariants(List<String> variants, BenchmarkGenerator generator) {
        if (variants.isEmpty() || variants.size() > ComplexityProfileLimits.MAX_VARIANTS) {
            throw new InvalidComplexityProfileException("invalid variant count");
        }
        for (String variant : variants) {
            if (!generator.supportedVariants().contains(variant)) {
                throw new InvalidComplexityProfileException("variant incompatible with generator: " + variant);
            }
        }
    }

    private List<VariableDefinition> parseVariables(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                throw new InvalidComplexityProfileException("variables must be array");
            }
            List<VariableDefinition> out = new ArrayList<>();
            for (JsonNode item : node) {
                out.add(new VariableDefinition(
                        text(item, "name"),
                        text(item, "meaning"),
                        text(item, "parameter"),
                        optionalText(item, "dimension")));
            }
            return out;
        } catch (InvalidComplexityProfileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidComplexityProfileException("malformed variables json");
        }
    }

    private Map<String, List<Integer>> parseLadder(String sizePlanJson) {
        try {
            JsonNode root = objectMapper.readTree(sizePlanJson);
            JsonNode ladder = root.get("ladder");
            if (ladder == null || !ladder.isObject()) {
                throw new InvalidComplexityProfileException("size ladder required");
            }
            Map<String, List<Integer>> map = new LinkedHashMap<>();
            ladder.fields().forEachRemaining(entry -> {
                List<Integer> values = new ArrayList<>();
                for (JsonNode n : entry.getValue()) {
                    values.add(n.asInt());
                }
                map.put(entry.getKey(), values);
            });
            return map;
        } catch (InvalidComplexityProfileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidComplexityProfileException("malformed size plan");
        }
    }

    private Map<String, Integer> parseMaxSizes(String sizePlanJson) {
        try {
            JsonNode root = objectMapper.readTree(sizePlanJson);
            JsonNode max = root.get("maxSizes");
            if (max == null || !max.isObject()) {
                throw new InvalidComplexityProfileException("maxSizes required");
            }
            Map<String, Integer> map = new LinkedHashMap<>();
            max.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asInt()));
            return map;
        } catch (InvalidComplexityProfileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidComplexityProfileException("malformed max sizes");
        }
    }

    private List<String> parseVariants(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode variants = root.get("variants");
            if (variants == null || !variants.isArray()) {
                throw new InvalidComplexityProfileException("variants required");
            }
            List<String> list = new ArrayList<>();
            variants.forEach(v -> list.add(v.asText()));
            return list;
        } catch (InvalidComplexityProfileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidComplexityProfileException("malformed variants json");
        }
    }

    private MeasurementLimits parseLimits(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return new MeasurementLimits(
                    node.path("warmups").asInt(0),
                    node.path("measuredRepeats").asInt(0),
                    node.path("perInvocationTimeoutMs").asInt(0),
                    node.path("maxTotalProfileMs").asInt(0));
        } catch (Exception ex) {
            throw new InvalidComplexityProfileException("malformed measurement limits");
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.asText().isBlank()) {
            throw new InvalidComplexityProfileException("missing field " + field);
        }
        return value.asText();
    }

    private static String optionalText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null ? null : value.asText();
    }

    public record MeasurementLimits(int warmups, int measuredRepeats, int perInvocationTimeoutMs, int maxTotalProfileMs) {
    }

    public record ParsedProfile(
            ComplexityBenchmarkProfile entity,
            List<VariableDefinition> variables,
            Map<String, List<Integer>> ladder,
            Map<String, Integer> maxSizes,
            List<String> variants,
            MeasurementLimits limits,
            BenchmarkGenerator generator) {
    }
}
