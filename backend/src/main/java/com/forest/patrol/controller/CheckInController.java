package com.forest.patrol.controller;

import com.forest.patrol.common.Result;
import com.forest.patrol.dto.CheckInDTO;
import com.forest.patrol.entity.CheckInRecord;
import com.forest.patrol.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "打卡管理")
@RestController
@RequestMapping("/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @Operation(summary = "获取班次打卡记录")
    @GetMapping("/shift/{shiftId}")
    public Result<List<CheckInRecord>> getByShiftId(@PathVariable Long shiftId) {
        return Result.success(checkInService.findByShiftId(shiftId));
    }

    @Operation(summary = "获取用户班次打卡记录")
    @GetMapping("/shift/{shiftId}/user/{userId}")
    public Result<List<CheckInRecord>> getByShiftAndUser(
            @PathVariable Long shiftId,
            @PathVariable Long userId) {
        return Result.success(checkInService.findByShiftIdAndUserId(shiftId, userId));
    }

    @Operation(summary = "打卡")
    @PostMapping
    public Result<CheckInRecord> checkIn(@Valid @RequestBody CheckInDTO dto) {
        return Result.success(checkInService.checkIn(dto));
    }

    @Operation(summary = "获取班次打卡数")
    @GetMapping("/shift/{shiftId}/count")
    public Result<Integer> countCheckPoints(@PathVariable Long shiftId) {
        return Result.success(checkInService.countCheckPoints(shiftId));
    }
}
