package com.hrishabh.problemservice.complexity.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
public class IntArrayWithTargetGenerator implements BenchmarkGenerator {

    private final ObjectMapper objectMapper;

    public IntArrayWithTargetGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String key() {
        return "INT_ARRAY_WITH_TARGET";
    }

    @Override
    public String version() {
        return "v1";
    }

    @Override
    public Set<String> supportedVariants() {
        return Set.of("RANDOM", "SORTED", "ADVERSARIAL");
    }

    @Override
    public ArrayNode generateInput(Map<String, Integer> sizeVector, String variant, long seedLong) {
        int n = sizeVector.getOrDefault("n", 0);
        if (n < 4) {
            throw new IllegalArgumentException("four-sum profile requires n >= 4");
        }
        Random random = new Random(seedLong);
        int[] nums = new int[n];
        switch (variant) {
            case "SORTED" -> {
                for (int i = 0; i < n; i++) {
                    nums[i] = i;
                }
            }
            case "ADVERSARIAL" -> {
                for (int i = 0; i < n; i++) {
                    nums[i] = (i % 2 == 0) ? -1_000_000 : 1_000_000;
                }
            }
            default -> {
                for (int i = 0; i < n; i++) {
                    nums[i] = random.nextInt(2001) - 1000;
                }
            }
        }
        int target = nums[0] + nums[1] + nums[2] + nums[3];
        ArrayNode args = objectMapper.createArrayNode();
        ArrayNode numsNode = objectMapper.createArrayNode();
        for (int value : nums) {
            numsNode.add(value);
        }
        args.add(numsNode);
        args.add(target);
        return args;
    }
}
