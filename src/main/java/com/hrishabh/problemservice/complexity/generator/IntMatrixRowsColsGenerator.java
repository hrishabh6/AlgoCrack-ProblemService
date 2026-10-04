package com.hrishabh.problemservice.complexity.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
public class IntMatrixRowsColsGenerator implements BenchmarkGenerator {

    private final ObjectMapper objectMapper;

    public IntMatrixRowsColsGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String key() {
        return "INT_MATRIX_ROWS_COLS";
    }

    @Override
    public String version() {
        return "v1";
    }

    @Override
    public Set<String> supportedVariants() {
        return Set.of("RANDOM", "FLAT", "PEAK");
    }

    @Override
    public ArrayNode generateInput(Map<String, Integer> sizeVector, String variant, long seedLong) {
        int rows = sizeVector.getOrDefault("n", 0);
        int cols = sizeVector.getOrDefault("m", 0);
        if (rows < 3 || cols < 3) {
            throw new IllegalArgumentException("matrix dimensions must be at least 3");
        }
        Random random = new Random(seedLong);
        ArrayNode matrix = objectMapper.createArrayNode();
        for (int r = 0; r < rows; r++) {
            ArrayNode row = objectMapper.createArrayNode();
            for (int c = 0; c < cols; c++) {
                row.add(cellValue(variant, random, r, c, rows, cols));
            }
            matrix.add(row);
        }
        ArrayNode args = objectMapper.createArrayNode();
        args.add(matrix);
        return args;
    }

    private static int cellValue(String variant, Random random, int r, int c, int rows, int cols) {
        return switch (variant) {
            case "FLAT" -> 1;
            case "PEAK" -> {
                int centerR = rows / 2;
                int centerC = cols / 2;
                yield 1000 - Math.abs(r - centerR) - Math.abs(c - centerC);
            }
            default -> random.nextInt(100_001);
        };
    }
}
