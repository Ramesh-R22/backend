package com.qrqueue.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qrqueue.backend.model.Counter;
import com.qrqueue.backend.model.QueueEntry;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findByCounter(Counter counter);

    List<QueueEntry> findByJoinedAtBetween(LocalDateTime start, LocalDateTime end);
}
