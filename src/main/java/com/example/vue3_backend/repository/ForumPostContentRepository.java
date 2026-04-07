package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumPostContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ForumPostContentRepository extends JpaRepository<ForumPostContent, Integer> {

    Optional<ForumPostContent> findByPostId(Integer postId);
}
