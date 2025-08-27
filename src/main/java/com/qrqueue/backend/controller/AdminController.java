package com.qrqueue.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qrqueue.backend.dto.CounterDto;
import com.qrqueue.backend.model.Counter;
import com.qrqueue.backend.model.Role;
import com.qrqueue.backend.model.User;
import com.qrqueue.backend.repository.CounterRepository;
import com.qrqueue.backend.repository.QueueEntryRepository;
import com.qrqueue.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private UserRepository userRepository;

    // ===== Counter Management =====
    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @PostMapping("/counters")
    public Counter createCounter(@RequestBody Counter counter) {
        return counterRepository.save(counter);
    }

    @GetMapping("/counters")
    public List<CounterDto> getAllCounters() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("/admin/counters accessed by user: {} with roles: {}", auth.getName(), auth.getAuthorities());
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

    @PutMapping("/counters/{id}/limit")
    public Counter updateCounterLimit(@PathVariable Long id, @RequestParam int dailyLimit) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Counter not found"));
        counter.setDailyLimit(dailyLimit);
        return counterRepository.save(counter);
    }

    @DeleteMapping("/counters/{id}")
    public void deleteCounter(@PathVariable Long id) {
        counterRepository.deleteById(id);
    }

    // ===== Staff Management =====

    @GetMapping("/staff")
    public List<User> getAllStaff() {
        return userRepository.findByRole(Role.STAFF);
    }

    @PostMapping("/staff")
    public User addStaff(@RequestBody User staff) {
        staff.setRole(Role.STAFF);
        return userRepository.save(staff);
    }

    @DeleteMapping("/staff/{id}")
    public void deleteStaff(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

        // Assign staff to counter
    @PutMapping("/counters/{id}/assign-staff")
    public Counter assignStaffToCounter(@PathVariable Long id, @RequestParam Long staffId) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Counter not found"));
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        if (staff.getRole() != Role.STAFF) {
            throw new RuntimeException("User is not a staff member");
        }
        counter.setAssignedStaff(staff);
        return counterRepository.save(counter);
    }
}
