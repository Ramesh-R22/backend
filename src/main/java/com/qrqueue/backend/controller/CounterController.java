package com.qrqueue.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qrqueue.backend.model.Counter;
import com.qrqueue.backend.model.User;
import com.qrqueue.backend.repository.CounterRepository;
import com.qrqueue.backend.repository.UserRepository;
import com.qrqueue.backend.service.CounterService;

@RestController
@RequestMapping("/counters")
public class CounterController {

    @Autowired
    private CounterService counterService;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private UserRepository userRepository;

    // Get the counter assigned to the currently logged-in staff
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('STAFF')")
    public Counter getAssignedCounter(Authentication authentication) {
    String username = authentication.getName();
    User staff = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Staff not found"));
    return counterRepository.findAll().stream()
        .filter(c -> c.getAssignedStaff() != null && c.getAssignedStaff().getId().equals(staff.getId()))
        .findFirst()
        .orElse(null);
    }
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Counter addCounter(@RequestBody Counter counter) {
        return counterService.addCounter(counter);
    }

    @PutMapping("/{id}/limit/{limit}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Counter setDailyLimit(@PathVariable Long id, @PathVariable int limit) {
        return counterService.updateDailyLimit(id, limit);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteCounter(@PathVariable Long id) {
        counterService.deleteCounter(id);
        return "Counter deleted";
    }
}
