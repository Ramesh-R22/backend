package com.qrqueue.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qrqueue.backend.model.Role;
import com.qrqueue.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
