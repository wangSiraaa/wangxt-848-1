package com.forest.patrol.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "incident_report")
public class IncidentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", insertable = false, updatable = false)
    private User reporter;

    @Column(name = "incident_type", nullable = false, length = 50)
    private String incidentType;

    @Column(nullable = false, length = 20)
    private String severity = Severity.LOW;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000)
    private String images;

    @Column(length = 20)
    private String status = Status.REPORTED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public interface IncidentType {
        String FIRE = "FIRE";
        String ILLEGAL = "ILLEGAL";
        String DAMAGE = "DAMAGE";
        String OTHER = "OTHER";
    }

    public interface Severity {
        String LOW = "LOW";
        String MEDIUM = "MEDIUM";
        String HIGH = "HIGH";
    }

    public interface Status {
        String REPORTED = "REPORTED";
        String PROCESSING = "PROCESSING";
        String RESOLVED = "RESOLVED";
        String CLOSED = "CLOSED";
    }
}
