package com.hrishabh.problemservice.complexity.generator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Set;

public interface BenchmarkGenerator {

    String key();

    String version();

    Set<String> supportedVariants();

    JsonNode generateInput(Map<String, Integer> sizeVector, String variant, long seedLong);
}
