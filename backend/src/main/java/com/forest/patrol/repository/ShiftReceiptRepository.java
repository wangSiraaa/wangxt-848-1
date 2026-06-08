package com.forest.patrol.repository;

import com.forest.patrol.entity.ShiftReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftReceiptRepository extends JpaRepository<ShiftReceipt, Long> {

    Optional<ShiftReceipt> findByShiftIdAndUserId(Long shiftId, Long userId);

    List<ShiftReceipt> findByShiftId(Long shiftId);

    List<ShiftReceipt> findByUserId(Long userId);

    List<ShiftReceipt> findByHandlerId(Long handlerId);

    boolean existsByShiftIdAndUserId(Long shiftId, Long userId);

    @Query("SELECT sr FROM ShiftReceipt sr WHERE sr.shiftId = :shiftId ORDER BY sr.createdAt DESC")
    List<ShiftReceipt> findByShiftIdOrderByCreatedAtDesc(@Param("shiftId") Long shiftId);
}
