package com.hrishabh.problemservice.complexity.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrishabh.problemservice.complexity.config.ComplexityProfileProperties;
import com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.ProfileMetadataResponse;
import com.hrishabh.problemservice.complexity.service.ComplexityProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalComplexityProfileControllerTest {

    @Mock
    private ComplexityProfileService profileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ComplexityProfileProperties properties = new ComplexityProfileProperties();
        InternalServiceAuth auth = new InternalServiceAuth(properties);
        InternalComplexityProfileController controller = new InternalComplexityProfileController(profileService, auth);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.hrishabh.problemservice.exceptions.GlobalExceptionHandler())
                .build();
    }

    @Test
    void profileResponseDoesNotIncludeReferenceOrHiddenTests() throws Exception {
        when(profileService.getActiveProfile(eq(10L), eq("JAVA"))).thenReturn(Optional.of(
                new ProfileMetadataResponse(
                        10L, "JAVA", "cbp-q10-java-v1", "FOUR_SUM_INT_ARRAY", "v1", "abc123",
                        "INT_ARRAY_WITH_TARGET", "v1",
                        List.of(new com.hrishabh.problemservice.complexity.dto.ComplexityProfileDtos.VariableDefinition(
                                "n", "length of nums", "nums", "length")),
                        Map.of("n", List.of(64)),
                        Map.of("n", 512),
                        List.of("RANDOM"),
                        3, 5, 1000, 15000)));

        mockMvc.perform(get("/api/v1/internal/questions/10/complexity-profile")
                        .param("language", "JAVA")
                        .header("X-Internal-Call", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatorKey").value("INT_ARRAY_WITH_TARGET"))
                .andExpect(jsonPath("$.referenceSolution").doesNotExist())
                .andExpect(jsonPath("$.testcases").doesNotExist());
    }

    @Test
    void rejectsMissingInternalHeaderWhenTokenNotConfigured() throws Exception {
        mockMvc.perform(get("/api/v1/internal/questions/10/complexity-profile")
                        .param("language", "JAVA"))
                .andExpect(status().isForbidden());
    }
}
