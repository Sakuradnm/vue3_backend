package com.example.vue3_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-check")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查 forum_main_categories 表
            List<Map<String, Object>> mainCategories = jdbcTemplate.queryForList(
                "SELECT COUNT(*) as count FROM forum_main_categories"
            );
            result.put("main_categories_count", mainCategories.get(0).get("count"));
            
            // 检查 forum_categories 表
            List<Map<String, Object>> categories = jdbcTemplate.queryForList(
                "SELECT COUNT(*) as count FROM forum_categories"
            );
            result.put("categories_count", categories.get(0).get("count"));
            
            // 查看 forum_categories 表结构
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM forum_categories"
            );
            result.put("forum_categories_columns", columns);
            
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("error_type", e.getClass().getName());
        }
        
        return result;
    }
}
