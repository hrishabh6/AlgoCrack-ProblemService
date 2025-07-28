package com.hrishabh.problemservice.controllers;

import com.hrishabh.problemservice.dto.CreateTagRequestDto;
import com.hrishabh.problemservice.service.TagServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagServiceImpl tagService;

    public TagController(TagServiceImpl tagService) {
        this.tagService = tagService;
    }


    @PostMapping
    public ResponseEntity<Void> addTag(@RequestBody CreateTagRequestDto dto) {
        tagService.addTag(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }
}
