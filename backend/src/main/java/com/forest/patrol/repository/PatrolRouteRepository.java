package com.forest.patrol.repository;

import com.forest.patrol.entity.PatrolRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatrolRouteRepository extends JpaRepository<PatrolRoute, Long> {
    List<PatrolRoute> findByStatus(Integer status);
    Optional<PatrolRoute> findByRouteCode(String routeCode);
}
