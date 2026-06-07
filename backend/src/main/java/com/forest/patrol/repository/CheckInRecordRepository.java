package com.forest.patrol.repository;

import com.forest.patrol.entity.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    List<CheckInRecord> findByShiftIdOrderByCheckpointOrder(Long shiftId);

    List<CheckInRecord> findByShiftIdAndUserIdOrderByCheckpointOrder(Long shiftId, Long userId);

    @Query("SELECT COUNT(cr) FROM CheckInRecord cr WHERE cr.shiftId = :shiftId")
    Integer countByShiftId(@Param("shiftId") Long shiftId);

    @Query("SELECT COUNT(DISTINCT cr.checkpointOrder) FROM CheckInRecord cr WHERE cr.shiftId = :shiftId")
    Integer countDistinctCheckpointsByShiftId(@Param("shiftId") Long shiftId);

    @Query("SELECT COUNT(DISTINCT cr.checkpointOrder) FROM CheckInRecord cr WHERE cr.shiftId = :shiftId AND cr.userId = :userId")
    Integer countDistinctCheckpointsByShiftIdAndUserId(@Param("shiftId") Long shiftId, @Param("userId") Long userId);

    boolean existsByShiftIdAndUserIdAndCheckpointOrder(Long shiftId, Long userId, Integer checkpointOrder);

    List<CheckInRecord> findByUserIdAndCheckInTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}
