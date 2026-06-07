package com.forest.patrol.controller;

import com.forest.patrol.common.Result;
import com.forest.patrol.entity.PatrolRoute;
import com.forest.patrol.service.PatrolRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "巡护路线管理")
@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class PatrolRouteController {

    private final PatrolRouteService patrolRouteService;

    @Operation(summary = "获取所有路线")
    @GetMapping
    public Result<List<PatrolRoute>> list() {
        return Result.success(patrolRouteService.findAll());
    }

    @Operation(summary = "获取启用的路线")
    @GetMapping("/active")
    public Result<List<PatrolRoute>> getActiveRoutes() {
        return Result.success(patrolRouteService.findActiveRoutes());
    }

    @Operation(summary = "获取路线详情")
    @GetMapping("/{id}")
    public Result<PatrolRoute> getById(@PathVariable Long id) {
        return patrolRouteService.findById(id)
                .map(Result::success)
                .orElse(Result.error("路线不存在"));
    }

    @Operation(summary = "新增路线")
    @PostMapping
    public Result<PatrolRoute> create(@RequestBody PatrolRoute route) {
        return Result.success(patrolRouteService.save(route));
    }

    @Operation(summary = "更新路线")
    @PutMapping("/{id}")
    public Result<PatrolRoute> update(@PathVariable Long id, @RequestBody PatrolRoute route) {
        route.setId(id);
        return Result.success(patrolRouteService.save(route));
    }

    @Operation(summary = "删除路线")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        patrolRouteService.delete(id);
        return Result.success();
    }
}
