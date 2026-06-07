package com.forest.patrol.controller;

import com.forest.patrol.common.Result;
import com.forest.patrol.entity.User;
import com.forest.patrol.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取所有用户")
    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.findAll());
    }

    @Operation(summary = "获取护林员列表")
    @GetMapping("/rangers")
    public Result<List<User>> getRangers() {
        return Result.success(userService.findRangers());
    }

    @Operation(summary = "按角色获取用户")
    @GetMapping("/role/{role}")
    public Result<List<User>> getByRole(@PathVariable String role) {
        return Result.success(userService.findByRole(role));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(Result::success)
                .orElse(Result.error("用户不存在"));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        return Result.success(userService.save(user));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return Result.success(userService.save(user));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
