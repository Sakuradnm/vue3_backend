package com.example.vue3_backend.service;

import com.example.vue3_backend.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // 获取所有用户
    List<User> findAll();

    // 根据 ID 查询
    Optional<User> findById(Long id);

    // 根据用户名查询
    Optional<User> findByUsername(String username);

    // 创建用户
    User createUser(User user);

    // 更新用户
    User updateUser(Long id, User user);

    // 删除用户
    void deleteUser(Long id);

    // 用户登录 - 支持手机号、邮箱或用户名，需验证级别
    Optional<User> loginWithIdentifier(String identifier, String password, User.Level level);

    // 根据级别查询
    List<User> findByLevel(User.Level level);
}
