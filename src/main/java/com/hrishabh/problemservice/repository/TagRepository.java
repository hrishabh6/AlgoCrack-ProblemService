package com.hrishabh.problemservice.repository;

import com.hrishabh.problemservice.dto.TagCountDto;
import com.hrishabh.problemservice.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(Collection<String> names);

    /** Every tag with the number of questions carrying it, most used first. */
    @Query("SELECT new com.hrishabh.problemservice.dto.TagCountDto(t.id, t.name, COUNT(q.id)) "
            + "FROM Tag t LEFT JOIN t.questions q GROUP BY t.id, t.name ORDER BY COUNT(q.id) DESC, t.name ASC")
    List<TagCountDto> findAllWithProblemCounts();
}
