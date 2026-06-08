package com.forest.patrol.controller;

import com.forest.patrol.common.Result;
import com.forest.patrol.dto.ShiftReceiptDTO;
import com.forest.patrol.entity.ShiftReceipt;
import com.forest.patrol.service.ShiftReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "处理回执管理")
@RestController
@RequestMapping("/shift-receipts")
@RequiredArgsConstructor
public class ShiftReceiptController {

    private final ShiftReceiptService shiftReceiptService;

    @Operation(summary = "获取所有处理回执")
    @GetMapping
    public Result<List<ShiftReceipt>> list() {
        return Result.success(shiftReceiptService.findAll());
    }

    @Operation(summary = "获取班次的处理回执列表")
    @GetMapping("/shift/{shiftId}")
    public Result<List<ShiftReceipt>> getByShiftId(@PathVariable Long shiftId) {
        return Result.success(shiftReceiptService.findByShiftId(shiftId));
    }

    @Operation(summary = "获取用户的处理回执列表")
    @GetMapping("/user/{userId}")
    public Result<List<ShiftReceipt>> getByUserId(@PathVariable Long userId) {
        return Result.success(shiftReceiptService.findByUserId(userId));
    }

    @Operation(summary = "获取班次用户的处理回执")
    @GetMapping("/shift/{shiftId}/user/{userId}")
    public Result<ShiftReceipt> getByShiftIdAndUserId(
            @PathVariable Long shiftId,
            @PathVariable Long userId) {
        return shiftReceiptService.findByShiftIdAndUserId(shiftId, userId)
                .map(Result::success)
                .orElse(Result.error("处理回执不存在"));
    }

    @Operation(summary = "获取处理回执详情")
    @GetMapping("/{id}")
    public Result<ShiftReceipt> getById(@PathVariable Long id) {
        return shiftReceiptService.findById(id)
                .map(Result::success)
                .orElse(Result.error("处理回执不存在"));
    }

    @Operation(summary = "创建处理回执（保存人员、班次、路线完成度）")
    @PostMapping
    public Result<ShiftReceipt> create(@Valid @RequestBody ShiftReceiptDTO dto) {
        return Result.success(shiftReceiptService.createReceipt(dto));
    }

    @Operation(summary = "快速创建处理回执（自动计算路线完成度）")
    @PostMapping("/quick")
    public Result<ShiftReceipt> createQuick(
            @RequestParam Long shiftId,
            @RequestParam Long userId,
            @RequestParam Long handlerId,
            @RequestParam(required = false) String handleRemark) {
        return Result.success(shiftReceiptService.createReceiptWithCheck(shiftId, userId, handlerId, handleRemark));
    }

    @Operation(summary = "更新处理回执")
    @PutMapping("/{id}")
    public Result<ShiftReceipt> update(
            @PathVariable Long id,
            @Valid @RequestBody ShiftReceiptDTO dto) {
        return Result.success(shiftReceiptService.updateReceipt(id, dto));
    }

    @Operation(summary = "删除处理回执")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shiftReceiptService.deleteReceipt(id);
        return Result.success();
    }
}
