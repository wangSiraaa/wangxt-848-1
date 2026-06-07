package com.forest.patrol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidentReportDTO {
    private Long shiftId;

    @NotNull(message = "上报人ID不能为空")
    private Long reporterId;

    @NotBlank(message = "异常类型不能为空")
    private String incidentType;

    @NotBlank(message = "严重程度不能为空")
    private String severity;

    @NotBlank(message = "位置不能为空")
    private String location;

    @NotBlank(message = "描述不能为空")
    private String description;

    private String images;
}
