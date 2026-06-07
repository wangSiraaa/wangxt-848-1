package com.forest.patrol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInDTO {
    @NotNull(message = "班次ID不能为空")
    private Long shiftId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "打卡点名称不能为空")
    private String checkpointName;

    @NotNull(message = "打卡点顺序不能为空")
    private Integer checkpointOrder;

    private String location;

    private String remark;
}
