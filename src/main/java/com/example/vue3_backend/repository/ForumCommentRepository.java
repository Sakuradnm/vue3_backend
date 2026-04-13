package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Integer> {

    @Query("SELECT c FROM ForumComment c LEFT JOIN FETCH c.user WHERE c.postId = :postId ORDER BY c.createdAt ASC")
    List<ForumComment> findByPostId(@Param("postId") Integer postId);

    @Query("SELECT c FROM ForumComment c LEFT JOIN FETCH c.user WHERE c.parentId = :parentId ORDER BY c.createdAt ASC")
    List<ForumComment> findByParentId(@Param("parentId") Integer parentId);

    @Query("SELECT c FROM ForumComment c LEFT JOIN FETCH c.user WHERE c.id = :id")
    Optional<ForumComment> findByIdWithUser(@Param("id") Integer id);
}
