package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {
    List<SubCategory> findByCategoryIdOrderBySortOrderAsc(Integer categoryId);
}
