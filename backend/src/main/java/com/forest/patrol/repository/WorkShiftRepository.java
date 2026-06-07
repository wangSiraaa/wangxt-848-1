package com.forest.patrol.repository;

import com.forest.patrol.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    List<WorkShift> findByShiftDate(LocalDate shiftDate);

    List<WorkShift> findByShiftDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT ws FROM WorkShift ws WHERE ws.id IN (SELECT su.shiftId FROM ShiftUser su WHERE su.userId = :userId) AND ws.shiftDate >= :startDate ORDER BY ws.shiftDate DESC")
    List<WorkShift> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(ws) > 0 FROM WorkShift ws WHERE ws.shiftDate = :shiftDate AND ws.shiftType = :shiftType AND ws.status != 'CANCELLED'")
    boolean existsByShiftDateAndShiftType(@Param("shiftDate") LocalDate shiftDate, @Param("shiftType") String shiftType);

    @Query("SELECT COUNT(su) FROM ShiftUser su WHERE su.shiftId = :shiftId")
    Integer countUsersByShiftId(@Param("shiftId") Long shiftId);

    List<WorkShift> findByFireRiskLevel(String fireRiskLevel);

    List<WorkShift> findByStatus(String status);
}
