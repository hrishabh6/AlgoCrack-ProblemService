package com.hrishabh.problemservice.semantic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.problemservice.complexity.model.ComplexityBenchmarkProfile;
import com.hrishabh.problemservice.semantic.service.SemanticProfileValidator.BenchmarkVariable;

import java.util.ArrayList;
import java.util.List;

final class SemanticBenchmarkAgreement {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SemanticBenchmarkAgreement() {
    }

    static List<BenchmarkVariable> variables(ComplexityBenchmarkProfile profile) {
        try {
            JsonNode node = MAPPER.readTree(profile.getVariableDefinitionsJson());
            List<BenchmarkVariable> variables = new ArrayList<>();
            if (!node.isArray()) {
                return List.of(new BenchmarkVariable("", "", "unreadable"));
            }
            for (JsonNode item : node) {
                String dimension = item.hasNonNull("dimension") ? item.get("dimension").asText() : null;
                variables.add(new BenchmarkVariable(
                        item.path("name").asText(),
                        item.path("parameter").asText(),
                        dimension));
            }
            return variables;
        } catch (Exception ex) {
            return List.of(new BenchmarkVariable("", "", "unreadable"));
        }
    }
}
