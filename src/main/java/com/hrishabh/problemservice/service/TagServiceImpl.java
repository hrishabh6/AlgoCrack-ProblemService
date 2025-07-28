package com.hrishabh.problemservice.service;

import com.hrishabh.algocrackentityservice.models.Tag;
import com.hrishabh.problemservice.dto.CreateTagRequestDto;
import com.hrishabh.problemservice.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl {

    private final TagRepository tagRepository;

    public void addTag(CreateTagRequestDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }

        if (tagRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Tag with name '" + dto.getName() + "' already exists");
        }

        Tag tag = Tag.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        tagRepository.save(tag);

    }
}
