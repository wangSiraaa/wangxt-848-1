package com.forest.patrol.service;

import com.forest.patrol.entity.PatrolRoute;
import com.forest.patrol.repository.PatrolRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolRouteService {

    private final PatrolRouteRepository patrolRouteRepository;

    public List<PatrolRoute> findAll() {
        return patrolRouteRepository.findAll();
    }

    public List<PatrolRoute> findActiveRoutes() {
        return patrolRouteRepository.findByStatus(1);
    }

    public Optional<PatrolRoute> findById(Long id) {
        return patrolRouteRepository.findById(id);
    }

    public Optional<PatrolRoute> findByRouteCode(String routeCode) {
        return patrolRouteRepository.findByRouteCode(routeCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public PatrolRoute save(PatrolRoute route) {
        return patrolRouteRepository.save(route);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        patrolRouteRepository.deleteById(id);
    }
}
