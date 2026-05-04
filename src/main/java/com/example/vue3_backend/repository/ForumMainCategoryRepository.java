package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ForumMainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForumMainCategoryRepository extends JpaRepository<ForumMainCategory, Integer> {
    List<ForumMainCategory> findAllByOrderBySortOrderAsc();
}
