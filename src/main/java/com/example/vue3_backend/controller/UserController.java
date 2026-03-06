package com.example.vue3_backend.controller;

import com.example.vue3_backend.entity.User;
import com.example.vue3_backend.common.Result;
import com.example.vue3_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class UserController {

    @Autowired
    private UserService userService;

    // 获取所有用户
    @GetMapping
    public ResponseEntity<Result<List<User>>> findAll() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(Result.success(users));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    // 根据 ID 查询
    @GetMapping("/{id}")
    public ResponseEntity<Result<User>> findById(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            return ResponseEntity.ok(Result.success(user));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(404, e.getMessage()));
        }
    }

    // 用户注册 - 接收 level、username、password、phone 四个参数
    @PostMapping("/register")
    public ResponseEntity<Result<User>> register(
            @RequestBody Map<String, String> registerData) {
        
        String levelStr = registerData.get("level");
        String username = registerData.get("username");
        String password = registerData.get("password");
        String phone = registerData.get("phone");
        
        System.out.println("=== 注册请求 ===");
        System.out.println("用户名：" + username);
        System.out.println("手机号：" + phone);
        System.out.println("级别：" + levelStr);
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "用户名不能为空"));
        }
        
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "密码不能为空"));
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "手机号不能为空"));
        }
        
        if (levelStr == null || levelStr.trim().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "用户级别不能为空"));
        }
        
        try {
            User.Level level;
            try {
                level = User.Level.valueOf(levelStr.toLowerCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.ok(Result.error(400, "无效的用户级别，应为 student 或 teacher"));
            }
            
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setPhone(phone);
            user.setEmail(username + "@example.com");
            user.setLevel(level);
            
            User createdUser = userService.createUser(user);
            System.out.println("注册成功 - 用户：" + createdUser.getUsername());
            return ResponseEntity.ok(Result.success(201, "注册成功", createdUser));
        } catch (RuntimeException e) {
            System.out.println("注册失败：" + e.getMessage());
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            System.out.println("注册异常：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Result.error(500, "注册失败，请稍后重试"));
        }
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<Result<User>> updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(Result.success("更新成功", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Result.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    // 用户登录 - 支持手机号、邮箱或用户名登录，需指定用户级别
    @PostMapping("/login")
    public ResponseEntity<Result<User>> login(
            @RequestBody Map<String, String> loginData) {

        String levelStr = loginData.get("level");
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        System.out.println("=== 登录请求 ===");
        System.out.println("标识符：" + username);
        System.out.println("级别：" + levelStr);
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "标识符不能为空"));
        }
        
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "密码不能为空"));
        }
        
        if (levelStr == null || levelStr.trim().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "用户级别不能为空"));
        }
        
        try {
            User.Level level = User.Level.valueOf(levelStr.toLowerCase());
            UserService.LoginResult loginResult = userService.loginWithIdentifier(username, password, level);
            
            if (loginResult.isSuccess()) {
                User user = loginResult.getUser().get();
                System.out.println("登录成功 - 用户：" + user.getUsername() + ", 级别：" + user.getLevel());
                return ResponseEntity.ok(Result.success(loginResult.getMessage(), user));
            } else {
                System.out.println("登录失败 - " + loginResult.getCode() + ": " + loginResult.getMessage());
                
                int statusCode = switch (loginResult.getCode()) {
                    case "PASSWORD_ERROR" -> 401;
                    case "LEVEL_MISMATCH" -> 403;
                    case "USER_NOT_FOUND" -> 404;
                    default -> 400;
                };
                
                return ResponseEntity.ok(Result.error(statusCode, loginResult.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("无效的级别参数：" + levelStr);
            return ResponseEntity.ok(Result.error(400, "无效的用户级别，应为 student 或 teacher"));
        } catch (Exception e) {
            System.out.println("登录异常：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Result.error(500, "登录失败，请稍后重试"));
        }
    }

    // 根据级别查询
    @GetMapping("/level/{level}")
    public ResponseEntity<Result<List<User>>> findByLevel(@PathVariable String level) {
        try {
            User.Level userLevel = User.Level.valueOf(level);
            List<User> users = userService.findByLevel(userLevel);
            return ResponseEntity.ok(Result.success(users));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(400, "无效的用户级别"));
        }
    }
}
