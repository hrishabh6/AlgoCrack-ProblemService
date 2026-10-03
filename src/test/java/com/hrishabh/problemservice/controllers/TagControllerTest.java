package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.TagCountDto;
import com.hrishabh.problemservice.exceptions.GlobalExceptionHandler;
import com.hrishabh.problemservice.service.TagServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagServiceImpl tagService;

    @Test
    void returnsTagsWithProblemCounts() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new TagController(tagService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        when(tagService.listTagsWithCounts()).thenReturn(List.of(
                new TagCountDto(1L, "Array", 4L),
                new TagCountDto(5L, "BFS", 0L)));

        mvc.perform(get("/api/v1/tags/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Array"))
                .andExpect(jsonPath("$[0].problemCount").value(4))
                .andExpect(jsonPath("$[1].problemCount").value(0));
    }
}
