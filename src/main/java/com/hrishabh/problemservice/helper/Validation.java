package com.hrishabh.problemservice.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.algocrackentityservice.models.QuestionMetadata;

import java.util.List;

/**
 * Validation helper for Problem Service.
 * 
 * Note: With the oracle-based judging architecture (Entity v2), expected output
 * validation is no longer needed here. Expected output is computed dynamically
 * by the reference solution (oracle) in the Submission Service.
 */
public class Validation {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Check if a JSON node is compatible with an expected type.
     */
    private boolean isTypeCompatible(String expectedType, JsonNode node) {
        switch (expectedType.toLowerCase()) {
            case "int":
            case "integer":
                return node.isInt() || node.isLong();
            case "double":
            case "float":
                return node.isDouble() || node.isFloat() || node.isInt();
            case "string":
                return node.isTextual();
            case "boolean":
                return node.isBoolean();
            case "list<int>":
            case "list<string>":
            case "list<boolean>":
            case "list<list<int>>":
            case "int[]":
            case "int[][]":
            case "string[]":
                return node.isArray();
            default:
                return true; // allow unhandled types
        }
    }

    /**
     * Validate testcase input against question metadata.
     * Only validates input structure matches the expected parameter types.
     * 
     * @param inputJson The JSON input string for the testcase
     * @param metadata  The question metadata containing parameter type definitions
     * @throws IllegalArgumentException if input is not valid JSON or doesn't match
     *                                  metadata
     */
    public void validateTestCaseInput(String inputJson, QuestionMetadata metadata) {
        try {
            JsonNode inputNode = OBJECT_MAPPER.readTree(inputJson);

            if (!inputNode.isArray()) {
                throw new IllegalArgumentException("Input must be a JSON array");
            }

            List<String> paramTypes = metadata.getParamTypes();
            if (inputNode.size() != paramTypes.size()) {
                throw new IllegalArgumentException("Input parameter count does not match metadata. Expected "
                        + paramTypes.size() + " but got " + inputNode.size());
            }

            for (int i = 0; i < paramTypes.size(); i++) {
                String expectedType = paramTypes.get(i);
                JsonNode actual = inputNode.get(i);

                if (!isTypeCompatible(expectedType, actual)) {
                    throw new IllegalArgumentException("Type mismatch at param index " + i
                            + ": expected " + expectedType + ", got " + actual.getNodeType());
                }
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in input", e);
        }
    }
}
