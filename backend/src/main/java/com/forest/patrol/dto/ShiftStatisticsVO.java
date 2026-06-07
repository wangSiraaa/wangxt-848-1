package com.forest.patrol.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShiftStatisticsVO {
    private Long shiftId;
    private String routeName;
    private Integer totalCheckpoints;
    private Integer completedCheckpoints;
    private BigDecimal completionRate;
    private String status;
}
