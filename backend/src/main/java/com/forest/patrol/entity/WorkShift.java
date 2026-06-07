package com.forest.patrol.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "work_shift")
public class WorkShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "shift_type", nullable = false, length = 20)
    private String shiftType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "fire_risk_level", nullable = false, length = 20)
    private String fireRiskLevel = FireRiskLevel.NORMAL;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", insertable = false, updatable = false)
    private PatrolRoute route;

    @Column(length = 20)
    private String status = Status.PENDING;

    @Column(name = "sign_in_time")
    private LocalDateTime signInTime;

    @Column(name = "sign_out_time")
    private LocalDateTime signOutTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public interface ShiftType {
        String DAY = "DAY";
        String NIGHT = "NIGHT";
    }

    public interface FireRiskLevel {
        String LOW = "LOW";
        String NORMAL = "NORMAL";
        String HIGH = "HIGH";
    }

    public interface Status {
        String PENDING = "PENDING";
        String IN_PROGRESS = "IN_PROGRESS";
        String COMPLETED = "COMPLETED";
        String CANCELLED = "CANCELLED";
    }
}
