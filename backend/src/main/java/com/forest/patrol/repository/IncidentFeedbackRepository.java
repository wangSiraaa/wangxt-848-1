package com.forest.patrol.repository;

import com.forest.patrol.entity.IncidentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentFeedbackRepository extends JpaRepository<IncidentFeedback, Long> {
    List<IncidentFeedback> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}
