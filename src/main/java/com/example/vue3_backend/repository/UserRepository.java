package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查询
    Optional<User> findByUsername(String username);

    // 根据手机号查询
    Optional<User> findByPhone(String phone);

    // 根据邮箱查询
    Optional<User> findByEmail(String email);

    // 根据用户级别查询
    List<User> findByLevel(User.Level level);

    // 模糊查询用户名
    List<User> findByUsernameContaining(String keyword);

}
