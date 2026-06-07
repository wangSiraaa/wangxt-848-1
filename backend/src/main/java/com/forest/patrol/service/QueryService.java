package com.forest.patrol.service;

import com.forest.patrol.dto.ShiftStatisticsVO;
import com.forest.patrol.entity.IncidentReport;
import com.forest.patrol.entity.WorkShift;
import com.forest.patrol.repository.IncidentReportRepository;
import com.forest.patrol.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {

    private final WorkShiftRepository workShiftRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final WorkShiftService workShiftService;
    private final IncidentService incidentService;

    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        List<WorkShift> todayShifts = workShiftRepository.findByShiftDate(today);

        int pendingCount = 0;
        int inProgressCount = 0;
        int completedCount = 0;

        for (WorkShift shift : todayShifts) {
            switch (shift.getStatus()) {
                case WorkShift.Status.PENDING -> pendingCount++;
                case WorkShift.Status.IN_PROGRESS -> inProgressCount++;
                case WorkShift.Status.COMPLETED -> completedCount++;
            }
        }

        result.put("todayTotal", todayShifts.size());
        result.put("todayPending", pendingCount);
        result.put("todayInProgress", inProgressCount);
        result.put("todayCompleted", completedCount);

        List<IncidentReport> pendingIncidents = incidentService.findPending();
        result.put("pendingIncidents", pendingIncidents.size());

        int highRiskCount = 0;
        int mediumRiskCount = 0;
        int lowRiskCount = 0;

        for (IncidentReport incident : pendingIncidents) {
            switch (incident.getSeverity()) {
                case IncidentReport.Severity.HIGH -> highRiskCount++;
                case IncidentReport.Severity.MEDIUM -> mediumRiskCount++;
                case IncidentReport.Severity.LOW -> lowRiskCount++;
            }
        }

        result.put("highRiskIncidents", highRiskCount);
        result.put("mediumRiskIncidents", mediumRiskCount);
        result.put("lowRiskIncidents", lowRiskCount);

        return result;
    }

    public Map<String, Object> getShiftDetail(Long shiftId) {
        Map<String, Object> result = new HashMap<>();

        WorkShift shift = workShiftRepository.findById(shiftId).orElse(null);
        if (shift != null) {
            result.put("shift", shift);

            ShiftStatisticsVO statistics = workShiftService.getShiftStatistics(shiftId);
            result.put("statistics", statistics);

            result.put("checkInRecords", workShiftService.getShiftUsers(shiftId));

            List<IncidentReport> incidents = incidentService.findByShiftId(shiftId);
            result.put("incidents", incidents);
        }

        return result;
    }

    public List<WorkShift> queryShifts(LocalDate startDate, LocalDate endDate, String status, String fireRiskLevel) {
        List<WorkShift> shifts = workShiftRepository.findByShiftDateBetween(startDate, endDate);

        if (status != null && !status.isEmpty()) {
            shifts = shifts.stream()
                    .filter(s -> status.equals(s.getStatus()))
                    .toList();
        }

        if (fireRiskLevel != null && !fireRiskLevel.isEmpty()) {
            shifts = shifts.stream()
                    .filter(s -> fireRiskLevel.equals(s.getFireRiskLevel()))
                    .toList();
        }

        return shifts;
    }

    public List<IncidentReport> queryIncidents(LocalDate startDate, LocalDate endDate, String status, String severity) {
        List<IncidentReport> incidents = incidentReportRepository
                .findByCreatedAtBetween(
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59));

        if (status != null && !status.isEmpty()) {
            incidents = incidents.stream()
                    .filter(i -> status.equals(i.getStatus()))
                    .toList();
        }

        if (severity != null && !severity.isEmpty()) {
            incidents = incidents.stream()
                    .filter(i -> severity.equals(i.getSeverity()))
                    .toList();
        }

        return incidents;
    }
}
