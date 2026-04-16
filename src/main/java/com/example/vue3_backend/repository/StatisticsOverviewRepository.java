package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.StatisticsOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatisticsOverviewRepository extends JpaRepository<StatisticsOverview, Long> {
    
    /**
     * 获取第一条统计数据（通常只有一条记录）
     */
    Optional<StatisticsOverview> findFirstByOrderByIdAsc();
}
