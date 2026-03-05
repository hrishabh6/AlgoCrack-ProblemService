package com.hrishabh.problemservice.service;

import com.hrishabh.problemservice.models.Tag;
import com.hrishabh.problemservice.dto.CreateTagRequestDto;
import com.hrishabh.problemservice.dto.TagResponseDto;
import com.hrishabh.problemservice.exceptions.ResourceNotFoundException;
import com.hrishabh.problemservice.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl {

    private final TagRepository tagRepository;

    /**
     * Add a new tag
     */
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

    /**
     * List all tags
     */
    public List<TagResponseDto> listTags() {
        return tagRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get tag by ID
     */
    public TagResponseDto getTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id " + id));
        return mapToDto(tag);
    }

    /**
     * Delete tag by ID
     */
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found with id " + id);
        }
        tagRepository.deleteById(id);
    }

    /**
     * Map Tag entity to DTO
     */
    private TagResponseDto mapToDto(Tag tag) {
        return TagResponseDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .build();
    }
}
