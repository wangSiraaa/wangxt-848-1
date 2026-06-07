package com.forest.patrol.controller;

import com.forest.patrol.common.Result;
import com.forest.patrol.dto.IncidentFeedbackDTO;
import com.forest.patrol.dto.IncidentReportDTO;
import com.forest.patrol.entity.IncidentFeedback;
import com.forest.patrol.entity.IncidentReport;
import com.forest.patrol.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "异常上报管理")
@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @Operation(summary = "获取所有异常")
    @GetMapping
    public Result<List<IncidentReport>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<IncidentReport> result;
        if (startDate != null && endDate != null) {
            result = incidentService.findByDateRange(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        } else if (status != null) {
            result = incidentService.findByStatus(status);
        } else if (severity != null) {
            result = incidentService.findBySeverity(severity);
        } else {
            result = incidentService.findAll();
        }
        return Result.success(result);
    }

    @Operation(summary = "获取待处理异常")
    @GetMapping("/pending")
    public Result<List<IncidentReport>> getPending() {
        return Result.success(incidentService.findPending());
    }

    @Operation(summary = "获取异常详情")
    @GetMapping("/{id}")
    public Result<IncidentReport> getById(@PathVariable Long id) {
        return incidentService.findById(id)
                .map(Result::success)
                .orElse(Result.error("异常记录不存在"));
    }

    @Operation(summary = "获取异常的处置反馈")
    @GetMapping("/{id}/feedbacks")
    public Result<List<IncidentFeedback>> getFeedbacks(@PathVariable Long id) {
        return Result.success(incidentService.getFeedbacks(id));
    }

    @Operation(summary = "上报异常")
    @PostMapping
    public Result<IncidentReport> report(@Valid @RequestBody IncidentReportDTO dto) {
        return Result.success(incidentService.reportIncident(dto));
    }

    @Operation(summary = "添加处置反馈")
    @PostMapping("/feedback")
    public Result<IncidentFeedback> addFeedback(@Valid @RequestBody IncidentFeedbackDTO dto) {
        return Result.success(incidentService.addFeedback(dto));
    }

    @Operation(summary = "更新异常状态")
    @PutMapping("/{id}/status")
    public Result<IncidentReport> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(incidentService.updateStatus(id, status));
    }
}
