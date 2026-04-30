package com.grain.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.grain.monitoring.common.Result;
import com.grain.monitoring.config.JwtUtil;
import com.grain.monitoring.dto.LoginRequest;
import com.grain.monitoring.dto.LoginResponse;
import com.grain.monitoring.dto.RegisterRequest;
import com.grain.monitoring.entity.User;
import com.grain.monitoring.service.UserService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                token);

        return Result.success(response);
    }

    @PostMapping("/auth/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        User exists = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername().trim()));

        if (exists != null) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword());
        user.setNickname(
                request.getNickname() == null || request.getNickname().trim().isEmpty()
                        ? request.getUsername().trim()
                        : request.getNickname().trim());

        boolean saved = userService.save(user);
        if (!saved) {
            return Result.error("注册失败，请稍后重试");
        }

        return Result.success();
    }
}
