package com.forest.patrol.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShiftReceiptDTO {

    @NotNull(message = "班次ID不能为空")
    private Long shiftId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "处理人ID不能为空")
    private Long handlerId;

    private String routeCompletion;

    private Integer totalCheckpoints;

    private Integer completedCheckpoints;

    private BigDecimal completionRate;

    private LocalDateTime signInTime;

    private LocalDateTime signOutTime;

    private String handleRemark;
}
