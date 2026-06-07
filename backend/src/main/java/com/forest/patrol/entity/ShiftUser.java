package com.forest.patrol.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shift_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"shift_id", "user_id"})
})
public class ShiftUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sign_in_time")
    private LocalDateTime signInTime;

    @Column(name = "sign_out_time")
    private LocalDateTime signOutTime;
}
