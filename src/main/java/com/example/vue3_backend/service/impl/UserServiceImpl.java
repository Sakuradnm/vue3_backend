package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.entity.User;
import com.example.vue3_backend.repository.UserRepository;
import com.example.vue3_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在，请使用其他用户名");
        }
        
        // 检查手机号是否已存在
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new RuntimeException("手机号已注册，请使用其他手机号或直接登录");
        }
        
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        existingUser.setUsername(user.getUsername());
        existingUser.setPhone(user.getPhone());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setLevel(user.getLevel());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserService.LoginResult loginWithIdentifier(String identifier, String password, User.Level level) {
        Optional<User> userOpt;
        
        if (identifier.matches("\\d{11}")) {
            userOpt = userRepository.findByPhone(identifier);
        } else if (identifier.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            userOpt = userRepository.findByEmail(identifier);
        } else {
            userOpt = userRepository.findByUsername(identifier);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (!user.getPassword().equals(password)) {
                return new UserService.LoginResult(false, "PASSWORD_ERROR", "密码错误", null);
            }
            
            if (user.getLevel() != level) {
                return new UserService.LoginResult(false, "LEVEL_MISMATCH", "用户级别与账号不匹配，当前账号级别：" + user.getLevel(), null);
            }
            
            return new UserService.LoginResult(true, "SUCCESS", "登录成功", user);
        }
        
        return new UserService.LoginResult(false, "USER_NOT_FOUND", "账号不存在", null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByLevel(User.Level level) {
        return userRepository.findByLevel(level);
    }
}
