package com.qrqueue.backend.controller;
import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qrqueue.backend.model.Counter;
import com.qrqueue.backend.repository.CounterRepository;
import com.qrqueue.backend.repository.QueueEntryRepository;

@RestController
@RequestMapping("/counters")
public class CountersController {

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @GetMapping("/all")
    public java.util.List<CounterWithRemaining> getAllCountersWithRemaining() {
        LocalDate today = LocalDate.now();
        return counterRepository.findAll().stream().map(counter -> {
            long used = queueEntryRepository.findByCounter(counter).stream()
                .filter(q -> q.getJoinedAt().toLocalDate().equals(today))
                .count();
            int remaining = counter.getDailyLimit() - (int)used;
            return new CounterWithRemaining(counter, remaining);
        }).collect(Collectors.toList());
    }

    public static class CounterWithRemaining {
        private final Counter counter;
        private final int remaining;
        public CounterWithRemaining(Counter counter, int remaining) {
            this.counter = counter;
            this.remaining = remaining;
        }
        public Long getId() { return counter.getId(); }
        public String getName() { return counter.getName(); }
        public int getDailyLimit() { return counter.getDailyLimit(); }
        public int getRemaining() { return remaining; }
    }
}
