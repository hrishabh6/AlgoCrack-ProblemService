package com.hrishabh.problemservice.repository;

import com.hrishabh.problemservice.models.ProblemListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemListItemRepository extends JpaRepository<ProblemListItem, Long> {

    boolean existsByListIdAndQuestionId(Long listId, Long questionId);

    long countByListId(Long listId);

    @Query("SELECT i.questionId FROM ProblemListItem i WHERE i.list.id = :listId ORDER BY i.createdAt DESC, i.id DESC")
    List<Long> findQuestionIdsByListId(@Param("listId") Long listId);

    /** Membership for every list a user owns in one query: rows of {listId, questionId}. */
    @Query("SELECT i.list.id, i.questionId FROM ProblemListItem i WHERE i.list.userId = :userId ORDER BY i.createdAt DESC, i.id DESC")
    List<Object[]> findMembershipByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM ProblemListItem i WHERE i.list.id = :listId AND i.questionId = :questionId")
    int deleteByListIdAndQuestionId(@Param("listId") Long listId, @Param("questionId") Long questionId);
}
