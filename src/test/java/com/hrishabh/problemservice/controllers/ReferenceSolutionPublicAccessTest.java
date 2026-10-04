package com.hrishabh.problemservice.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferenceSolutionPublicAccessTest {

    @Test
    void publicReferenceSolutionControllerDoesNotExposeGetMapping() {
        boolean hasGet = Arrays.stream(ReferenceSolutionController.class.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(GetMapping.class));
        assertTrue(!hasGet, "public reference solution must not expose browser-readable GET");
    }
}
