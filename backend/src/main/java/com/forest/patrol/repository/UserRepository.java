package com.forest.patrol.repository;

import com.forest.patrol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(String role);
    List<User> findByRoleAndStatus(String role, Integer status);
    Optional<User> findByUsername(String username);
}
