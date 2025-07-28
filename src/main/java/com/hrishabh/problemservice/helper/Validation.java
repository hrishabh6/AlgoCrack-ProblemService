package com.hrishabh.problemservice.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.algocrackentityservice.models.QuestionMetadata;

import java.util.List;

public class Validation {

    private boolean isTypeCompatible(String expectedType, JsonNode node) {
        switch (expectedType.toLowerCase()) {
            case "int":
            case "integer":
                return node.isInt() || node.isLong();
            case "double":
            case "float":
                return node.isDouble() || node.isFloat() || node.isInt(); // allow 5.0 to match double
            case "string":
                return node.isTextual();
            case "boolean":
                return node.isBoolean();
            case "list<int>":
            case "list<string>":
            case "list<boolean>":
                return node.isArray(); // basic check, not full element-level type
            default:
                return true; // allow unhandled types
        }
    }



    public void validateTestCaseInputAndOutput(String inputJson, String outputJson, QuestionMetadata metadata) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Parse input JSON string to array
            JsonNode inputNode = mapper.readTree(inputJson);

            if (!inputNode.isArray()) {
                throw new IllegalArgumentException("Input must be a JSON array");
            }

            List<String> paramTypes = metadata.getParamTypes();
            if (inputNode.size() != paramTypes.size()) {
                throw new IllegalArgumentException("Input parameter count does not match metadata");
            }

            for (int i = 0; i < paramTypes.size(); i++) {
                String expectedType = paramTypes.get(i);
                JsonNode actual = inputNode.get(i);

                if (!isTypeCompatible(expectedType, actual)) {
                    throw new IllegalArgumentException("Type mismatch at param index " + i + ": expected " + expectedType + ", got " + actual);
                }
            }

            // Validate expected output
            JsonNode outputNode = mapper.readTree(outputJson);
            String expectedReturnType = metadata.getReturnType();

            if (!isTypeCompatible(expectedReturnType, outputNode)) {
                throw new IllegalArgumentException("Return type mismatch. Expected: " + expectedReturnType);
            }

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in input/output", e);
        }
    }

}
