package com.forest.patrol.repository;

import com.forest.patrol.entity.ShiftUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftUserRepository extends JpaRepository<ShiftUser, Long> {
    Optional<ShiftUser> findByShiftIdAndUserId(Long shiftId, Long userId);

    List<ShiftUser> findByShiftId(Long shiftId);

    List<ShiftUser> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM ShiftUser su WHERE su.shiftId = :shiftId")
    void deleteByShiftId(@Param("shiftId") Long shiftId);

    @Modifying
    @Query("UPDATE ShiftUser su SET su.signInTime = :signInTime WHERE su.shiftId = :shiftId AND su.userId = :userId")
    int updateSignInTime(@Param("shiftId") Long shiftId, @Param("userId") Long userId, @Param("signInTime") LocalDateTime signInTime);

    @Modifying
    @Query("UPDATE ShiftUser su SET su.signOutTime = :signOutTime WHERE su.shiftId = :shiftId AND su.userId = :userId")
    int updateSignOutTime(@Param("shiftId") Long shiftId, @Param("userId") Long userId, @Param("signOutTime") LocalDateTime signOutTime);

    @Query("SELECT COUNT(su) FROM ShiftUser su WHERE su.shiftId = :shiftId AND su.signOutTime IS NOT NULL")
    Integer countSignedOutUsers(@Param("shiftId") Long shiftId);
}
