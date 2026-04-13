package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {

    @Query("SELECT f FROM ForumPost f LEFT JOIN FETCH f.user WHERE f.category = :category")
    List<ForumPost> findByCategory(@Param("category") String category);

    @Query("SELECT f FROM ForumPost f LEFT JOIN FETCH f.user WHERE f.category = :category OR :category IS NULL ORDER BY f.pinned DESC, f.score DESC")
    List<ForumPost> findByCategoryOrderByPinnedAndScore(@Param("category") String category);

    @Query("SELECT f FROM ForumPost f LEFT JOIN FETCH f.user WHERE (:category IS NULL OR f.category = :category) AND (LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.preview) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY f.pinned DESC, f.score DESC")
    List<ForumPost> searchPosts(@Param("category") String category, @Param("keyword") String keyword);

    @Query("SELECT f FROM ForumPost f LEFT JOIN FETCH f.user ORDER BY f.pinned DESC, f.score DESC")
    List<ForumPost> findAllOrderByPinnedAndScore();

    @Query("SELECT f FROM ForumPost f LEFT JOIN FETCH f.user WHERE f.id = :id")
    Optional<ForumPost> findByIdWithUser(@Param("id") Integer id);
}
