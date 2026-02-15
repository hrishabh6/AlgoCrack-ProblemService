package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.CreateTagRequestDto;
import com.hrishabh.problemservice.dto.TagResponseDto;
import com.hrishabh.problemservice.service.TagServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagServiceImpl tagService;

    /**
     * Create a new tag
     */
    @PostMapping
    public ResponseEntity<Void> addTag(@Valid @RequestBody CreateTagRequestDto dto) {
        tagService.addTag(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * List all tags
     */
    @GetMapping
    public ResponseEntity<List<TagResponseDto>> listTags() {
        return ResponseEntity.ok(tagService.listTags());
    }

    /**
     * Get tag by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTag(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTag(id));
    }

    /**
     * Delete tag by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
