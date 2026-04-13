package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Integer> {
    
    Optional<ForumPostLike> findByPostIdAndUserId(Integer postId, Integer userId);
    
    void deleteByPostIdAndUserId(Integer postId, Integer userId);
}
