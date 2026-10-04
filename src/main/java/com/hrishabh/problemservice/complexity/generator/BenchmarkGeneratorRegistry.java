package com.hrishabh.problemservice.complexity.generator;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BenchmarkGeneratorRegistry {

    private final Map<String, BenchmarkGenerator> byKey = new HashMap<>();

    public BenchmarkGeneratorRegistry(List<BenchmarkGenerator> generators) {
        for (BenchmarkGenerator generator : generators) {
            byKey.put(generator.key(), generator);
        }
    }

    public Optional<BenchmarkGenerator> find(String generatorKey, String generatorVersion) {
        BenchmarkGenerator generator = byKey.get(generatorKey);
        if (generator == null || !generator.version().equals(generatorVersion)) {
            return Optional.empty();
        }
        return Optional.of(generator);
    }
}
