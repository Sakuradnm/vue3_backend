package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ForumCommentLikeRepository extends JpaRepository<ForumCommentLike, Integer> {
    
    Optional<ForumCommentLike> findByCommentIdAndUserId(Integer commentId, Long userId);
    
    void deleteByCommentIdAndUserId(Integer commentId, Long userId);

    void deleteByCommentId(Integer commentId);
}
