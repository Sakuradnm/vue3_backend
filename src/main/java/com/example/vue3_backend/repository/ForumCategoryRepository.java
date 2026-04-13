package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Integer> {
    List<ForumCategory> findAllByOrderBySortOrderAsc();
    Optional<ForumCategory> findByLabel(String label);
}
