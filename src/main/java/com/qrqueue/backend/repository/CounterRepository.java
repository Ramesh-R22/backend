package com.qrqueue.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qrqueue.backend.model.Counter;

public interface CounterRepository extends JpaRepository<Counter, Long> {
}
