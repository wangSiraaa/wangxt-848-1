package com.forest.patrol.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ShiftCreateDTO {
    @NotNull(message = "值班日期不能为空")
    private LocalDate shiftDate;

    @NotNull(message = "班次类型不能为空")
    private String shiftType;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotNull(message = "火险等级不能为空")
    private String fireRiskLevel;

    @NotNull(message = "巡护路线不能为空")
    private Long routeId;

    @NotEmpty(message = "值班人员不能为空")
    private List<Long> userIds;
}
