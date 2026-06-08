package com.forest.patrol.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shift_receipt", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"shift_id", "user_id"})
})
public class ShiftReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "handler_id", nullable = false)
    private Long handlerId;

    @Column(name = "route_completion", nullable = false, length = 20)
    private String routeCompletion = RouteCompletion.NOT_STARTED;

    @Column(name = "total_checkpoints", nullable = false)
    private Integer totalCheckpoints = 0;

    @Column(name = "completed_checkpoints", nullable = false)
    private Integer completedCheckpoints = 0;

    @Column(name = "completion_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(name = "sign_in_time")
    private LocalDateTime signInTime;

    @Column(name = "sign_out_time")
    private LocalDateTime signOutTime;

    @Column(name = "handle_remark", length = 500)
    private String handleRemark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", insertable = false, updatable = false)
    private WorkShift shift;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id", insertable = false, updatable = false)
    private User handler;

    public interface RouteCompletion {
        String COMPLETED = "COMPLETED";
        String PARTIAL = "PARTIAL";
        String NOT_STARTED = "NOT_STARTED";
    }
}
