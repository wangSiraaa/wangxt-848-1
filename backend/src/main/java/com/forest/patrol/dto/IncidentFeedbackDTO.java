package com.forest.patrol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidentFeedbackDTO {
    @NotNull(message = "异常ID不能为空")
    private Long incidentId;

    @NotNull(message = "处理人ID不能为空")
    private Long handlerId;

    @NotBlank(message = "处置措施不能为空")
    private String actionTaken;

    private String result;

    private String status;
}
