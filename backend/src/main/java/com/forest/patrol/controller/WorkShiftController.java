package com.forest.patrol.controller;

import com.forest.patrol.common.Result;
import com.forest.patrol.dto.ShiftCreateDTO;
import com.forest.patrol.dto.ShiftStatisticsVO;
import com.forest.patrol.dto.SignInOutDTO;
import com.forest.patrol.entity.WorkShift;
import com.forest.patrol.service.WorkShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "班次管理")
@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    @Operation(summary = "获取所有班次")
    @GetMapping
    public Result<List<WorkShift>> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<WorkShift> result;
        if (date != null) {
            result = workShiftService.findByDate(date);
        } else if (startDate != null && endDate != null) {
            result = workShiftService.findByDateRange(startDate, endDate);
        } else {
            result = workShiftService.findAll();
        }
        return Result.success(result);
    }

    @Operation(summary = "获取用户的班次")
    @GetMapping("/user/{userId}")
    public Result<List<WorkShift>> getByUser(@PathVariable Long userId) {
        return Result.success(workShiftService.findByUserId(userId));
    }

    @Operation(summary = "获取班次详情")
    @GetMapping("/{id}")
    public Result<WorkShift> getById(@PathVariable Long id) {
        return workShiftService.findById(id)
                .map(Result::success)
                .orElse(Result.error("班次不存在"));
    }

    @Operation(summary = "获取班次统计")
    @GetMapping("/{id}/statistics")
    public Result<ShiftStatisticsVO> getStatistics(@PathVariable Long id) {
        return Result.success(workShiftService.getShiftStatistics(id));
    }

    @Operation(summary = "创建班次")
    @PostMapping
    public Result<WorkShift> create(@Valid @RequestBody ShiftCreateDTO dto) {
        return Result.success(workShiftService.createShift(dto));
    }

    @Operation(summary = "更新班次")
    @PutMapping("/{id}")
    public Result<WorkShift> update(@PathVariable Long id, @Valid @RequestBody ShiftCreateDTO dto) {
        return Result.success(workShiftService.updateShift(id, dto));
    }

    @Operation(summary = "签到")
    @PostMapping("/sign-in")
    public Result<Void> signIn(@Valid @RequestBody SignInOutDTO dto) {
        workShiftService.signIn(dto);
        return Result.success();
    }

    @Operation(summary = "签退")
    @PostMapping("/sign-out")
    public Result<Void> signOut(@Valid @RequestBody SignInOutDTO dto) {
        workShiftService.signOut(dto);
        return Result.success();
    }

    @Operation(summary = "取消班次")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        workShiftService.cancelShift(id);
        return Result.success();
    }
}
