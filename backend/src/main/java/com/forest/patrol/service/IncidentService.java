package com.forest.patrol.service;

import com.forest.patrol.common.BusinessException;
import com.forest.patrol.dto.IncidentFeedbackDTO;
import com.forest.patrol.dto.IncidentReportDTO;
import com.forest.patrol.entity.IncidentFeedback;
import com.forest.patrol.entity.IncidentReport;
import com.forest.patrol.repository.IncidentFeedbackRepository;
import com.forest.patrol.repository.IncidentReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class IncidentService {

    private final IncidentReportRepository incidentReportRepository;
    private final IncidentFeedbackRepository incidentFeedbackRepository;

    public List<IncidentReport> findAll() {
        return incidentReportRepository.findAll();
    }

    public List<IncidentReport> findByStatus(String status) {
        return incidentReportRepository.findByStatus(status);
    }

    public List<IncidentReport> findBySeverity(String severity) {
        return incidentReportRepository.findBySeverity(severity);
    }

    public List<IncidentReport> findPending() {
        return incidentReportRepository.findByStatusNotIn(
                Arrays.asList(IncidentReport.Status.RESOLVED, IncidentReport.Status.CLOSED));
    }

    public Optional<IncidentReport> findById(Long id) {
        return incidentReportRepository.findById(id);
    }

    public List<IncidentReport> findByShiftId(Long shiftId) {
        return incidentReportRepository.findByShiftId(shiftId);
    }

    public List<IncidentReport> findByReporterId(Long reporterId) {
        return incidentReportRepository.findByReporterId(reporterId);
    }

    public List<IncidentReport> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return incidentReportRepository.findByCreatedAtBetween(startTime, endTime);
    }

    public IncidentReport reportIncident(IncidentReportDTO dto) {
        IncidentReport report = new IncidentReport();
        report.setShiftId(dto.getShiftId());
        report.setReporterId(dto.getReporterId());
        report.setIncidentType(dto.getIncidentType());
        report.setSeverity(dto.getSeverity());
        report.setLocation(dto.getLocation());
        report.setDescription(dto.getDescription());
        report.setImages(dto.getImages());
        report.setStatus(IncidentReport.Status.REPORTED);

        report = incidentReportRepository.save(report);
        log.info("异常上报成功: incidentId={}, type={}, severity={}",
                report.getId(), dto.getIncidentType(), dto.getSeverity());
        return report;
    }

    public IncidentFeedback addFeedback(IncidentFeedbackDTO dto) {
        IncidentReport report = incidentReportRepository.findById(dto.getIncidentId())
                .orElseThrow(() -> new BusinessException("异常记录不存在"));

        if (IncidentReport.Status.CLOSED.equals(report.getStatus())) {
            throw new BusinessException("该异常已关闭，无法添加反馈");
        }

        IncidentFeedback feedback = new IncidentFeedback();
        feedback.setIncidentId(dto.getIncidentId());
        feedback.setHandlerId(dto.getHandlerId());
        feedback.setActionTaken(dto.getActionTaken());
        feedback.setResult(dto.getResult());

        feedback = incidentFeedbackRepository.save(feedback);

        if (dto.getStatus() != null) {
            report.setStatus(dto.getStatus());
            incidentReportRepository.save(report);
        } else if (IncidentReport.Status.REPORTED.equals(report.getStatus())) {
            report.setStatus(IncidentReport.Status.PROCESSING);
            incidentReportRepository.save(report);
        }

        log.info("添加处置反馈成功: incidentId={}, handlerId={}", dto.getIncidentId(), dto.getHandlerId());
        return feedback;
    }

    public List<IncidentFeedback> getFeedbacks(Long incidentId) {
        return incidentFeedbackRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);
    }

    public IncidentReport updateStatus(Long id, String status) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("异常记录不存在"));
        report.setStatus(status);
        return incidentReportRepository.save(report);
    }
}
