package com.hrishabh.problemservice.repository;

import com.hrishabh.problemservice.models.Question;
import com.hrishabh.problemservice.models.Tag;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class QuestionSpecification {

    public static Specification<Question> hasDifficulty(String difficulty) {
        return (root, query, cb) -> difficulty == null || difficulty.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("difficultyLevel")), difficulty.trim().toLowerCase(Locale.ROOT));
    }

    public static Specification<Question> lacksDifficulty(String difficulty) {
        return (root, query, cb) -> difficulty == null || difficulty.isBlank()
                ? null
                : cb.notEqual(cb.lower(root.get("difficultyLevel")), difficulty.trim().toLowerCase(Locale.ROOT));
    }

    public static Specification<Question> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null || tagName.isBlank())
                return null;
            Join<Question, Tag> tagJoin = root.join("tags");
            return cb.equal(tagJoin.get("name"), tagName);
        };
    }

    /**
     * Matches questions that carry at least one of the given tags (union). Uses a single EXISTS
     * subquery so paging counts stay correct and rows are not duplicated.
     */
    public static Specification<Question> hasAnyOfTags(List<String> tagNames) {
        return (root, query, cb) -> {
            if (tagNames == null || tagNames.isEmpty())
                return null;
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Question> subRoot = sub.from(Question.class);
            Join<Question, Tag> subTags = subRoot.join("tags");
            sub.select(subRoot.get("id"))
                    .where(cb.equal(subRoot.get("id"), root.get("id")),
                            subTags.get("name").in(tagNames));
            return cb.exists(sub);
        };
    }

    /**
     * Matches questions that carry none of the given tags. Dual of {@link #hasAnyOfTags}.
     */
    public static Specification<Question> hasNoneOfTags(List<String> tagNames) {
        return (root, query, cb) -> {
            if (tagNames == null || tagNames.isEmpty())
                return null;
            Predicate[] predicates = tagNames.stream().map(tagName -> {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Question> subRoot = sub.from(Question.class);
                Join<Question, Tag> subTags = subRoot.join("tags");
                sub.select(subRoot.get("id"))
                        .where(cb.equal(subRoot.get("id"), root.get("id")),
                                cb.equal(subTags.get("name"), tagName));
                return cb.not(cb.exists(sub));
            }).toArray(Predicate[]::new);
            return cb.and(predicates);
        };
    }

    public static Specification<Question> titleContains(String search) {
        return (root, query, cb) -> search == null || search.isBlank()
                ? null
                : cb.like(cb.lower(root.get("questionTitle")), "%" + search.toLowerCase() + "%");
    }

    /**
     * Title substring match, or exact problem number when the search term is numeric.
     */
    public static Specification<Question> titleOrIdMatches(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank())
                return null;
            String term = search.trim();
            Predicate title = cb.like(cb.lower(root.get("questionTitle")),
                    "%" + escapeLike(term.toLowerCase(Locale.ROOT)) + "%", '\\');
            if (term.matches("\\d{1,18}")) {
                return cb.or(title, cb.equal(root.get("id"), Long.parseLong(term)));
            }
            return title;
        };
    }

    public static Specification<Question> hasCompany(String company) {
        return (root, query, cb) -> company == null || company.isBlank()
                ? null
                : cb.equal(root.get("company"), company);
    }

    public static Specification<Question> idIn(Collection<Long> ids) {
        return (root, query, cb) -> ids == null ? null : root.get("id").in(ids);
    }

    public static Specification<Question> idNotIn(Collection<Long> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty() ? null : cb.not(root.get("id").in(ids));
    }

    /**
     * Orders by difficulty (Easy, Medium, Hard, then anything else) and then by id.
     * Skipped for count queries, which must not carry an ORDER BY.
     */
    public static Specification<Question> orderByDifficulty(boolean ascending) {
        return (root, query, cb) -> {
            Class<?> resultType = query.getResultType();
            if (resultType != Long.class && resultType != long.class) {
                Expression<Integer> rank = cb.<Integer>selectCase()
                        .when(cb.equal(cb.lower(root.get("difficultyLevel")), "easy"), 1)
                        .when(cb.equal(cb.lower(root.get("difficultyLevel")), "medium"), 2)
                        .when(cb.equal(cb.lower(root.get("difficultyLevel")), "hard"), 3)
                        .otherwise(4);
                query.orderBy(ascending ? cb.asc(rank) : cb.desc(rank), cb.asc(root.get("id")));
            }
            return null;
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
