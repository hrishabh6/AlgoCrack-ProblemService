package com.hrishabh.problemservice.repository;

import com.hrishabh.algocrackentityservice.models.Question;
import com.hrishabh.algocrackentityservice.models.Tag;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class QuestionSpecification {

    public static Specification<Question> hasDifficulty(String difficulty) {
        return (root, query, cb) -> difficulty == null || difficulty.isBlank()
                ? null
                : cb.equal(root.get("difficultyLevel"), difficulty);
    }

    public static Specification<Question> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null || tagName.isBlank())
                return null;
            Join<Question, Tag> tagJoin = root.join("tags");
            return cb.equal(tagJoin.get("name"), tagName);
        };
    }

    public static Specification<Question> titleContains(String search) {
        return (root, query, cb) -> search == null || search.isBlank()
                ? null
                : cb.like(cb.lower(root.get("questionTitle")), "%" + search.toLowerCase() + "%");
    }

    public static Specification<Question> hasCompany(String company) {
        return (root, query, cb) -> company == null || company.isBlank()
                ? null
                : cb.equal(root.get("company"), company);
    }
}
