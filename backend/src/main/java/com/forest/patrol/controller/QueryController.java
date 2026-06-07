package com.forest.patrol.controller;

import com.forest.patrol.common.Result;
import com.forest.patrol.entity.IncidentReport;
import com.forest.patrol.entity.WorkShift;
import com.forest.patrol.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "查询统计")
@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    @Operation(summary = "获取首页统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(queryService.getDashboard());
    }

    @Operation(summary = "获取班次详情（包含打卡记录和异常）")
    @GetMapping("/shift/{shiftId}")
    public Result<Map<String, Object>> getShiftDetail(@PathVariable Long shiftId) {
        return Result.success(queryService.getShiftDetail(shiftId));
    }

    @Operation(summary = "查询班次")
    @GetMapping("/shifts")
    public Result<List<WorkShift>> queryShifts(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fireRiskLevel) {
        return Result.success(queryService.queryShifts(startDate, endDate, status, fireRiskLevel));
    }

    @Operation(summary = "查询异常")
    @GetMapping("/incidents")
    public Result<List<IncidentReport>> queryIncidents(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        return Result.success(queryService.queryIncidents(startDate, endDate, status, severity));
    }
}
