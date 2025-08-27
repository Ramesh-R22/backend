package com.qrqueue.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qrqueue.backend.dto.CounterDto;
import com.qrqueue.backend.model.Counter;
import com.qrqueue.backend.repository.CounterRepository;
import com.qrqueue.backend.repository.QueueEntryRepository;
@Service

public class CounterService {

    @Autowired
    private CounterRepository counterRepository;


    @Autowired
    private QueueEntryRepository queueEntryRepository;

    public Counter addCounter(Counter counter) {
        return counterRepository.save(counter);
    }

    // Optional: Provide CounterDto with waiting count for all counters
    public List<CounterDto> getAllCounterDtos() {
        return counterRepository.findAll().stream().map(counter -> {
            int waiting = (int) queueEntryRepository.findByCounter(counter).stream().filter(q -> !q.isServed()).count();
            return new CounterDto(
                counter.getId(),
                counter.getName(),
                counter.getDailyLimit(),
                waiting,
                counter.getAssignedStaff()
            );
        }).collect(Collectors.toList());
    }

    public Counter updateDailyLimit(Long counterId, int limit) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new RuntimeException("Counter not found"));
        counter.setDailyLimit(limit);
        return counterRepository.save(counter);
    }

    public List<Counter> getAllCounters() {
        return counterRepository.findAll();
    }

    public void deleteCounter(Long counterId) {
        counterRepository.deleteById(counterId);
    }
}
