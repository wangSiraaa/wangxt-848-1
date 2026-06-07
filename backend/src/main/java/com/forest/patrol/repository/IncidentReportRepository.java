package com.forest.patrol.repository;

import com.forest.patrol.entity.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    List<IncidentReport> findByStatus(String status);

    List<IncidentReport> findBySeverity(String severity);

    List<IncidentReport> findByReporterId(Long reporterId);

    List<IncidentReport> findByShiftId(Long shiftId);

    List<IncidentReport> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<IncidentReport> findByStatusNotIn(List<String> statuses);
}
