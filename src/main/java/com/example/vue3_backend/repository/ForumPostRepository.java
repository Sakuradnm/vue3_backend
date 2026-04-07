package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {

    List<ForumPost> findByCategory(String category);

    @Query("SELECT f FROM ForumPost f WHERE f.category = :category OR :category IS NULL ORDER BY f.pinned DESC, f.score DESC")
    List<ForumPost> findByCategoryOrderByPinnedAndScore(@Param("category") String category);

    @Query("SELECT f FROM ForumPost f WHERE (:category IS NULL OR f.category = :category) AND (LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.preview) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY f.pinned DESC, f.score DESC")
    List<ForumPost> searchPosts(@Param("category") String category, @Param("keyword") String keyword);

    @Query("SELECT f FROM ForumPost f ORDER BY f.pinned DESC, f.score DESC")
    List<ForumPost> findAllOrderByPinnedAndScore();
}
