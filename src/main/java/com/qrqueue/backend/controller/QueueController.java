package com.qrqueue.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qrqueue.backend.dto.JoinQueueRequest;
import com.qrqueue.backend.model.QueueEntry;
import com.qrqueue.backend.service.QueueService;

@RestController
@RequestMapping("/queue")
public class QueueController {

    @Autowired
    private QueueService queueService;

    @PostMapping("/join/{counterId}")
    public QueueEntry joinQueue(@PathVariable Long counterId, @RequestBody JoinQueueRequest request) {
        return queueService.addToQueue(counterId, request.getUserName());
    }

    @GetMapping("/{counterId}")
    public List<QueueEntry> getQueueByCounter(@PathVariable Long counterId) {
        return queueService.getQueueByCounter(counterId);
    }

    @GetMapping("/entry/{id}")
    public Object getQueueEntryById(@PathVariable Long id) {
        return queueService.getQueueEntryById(id);
    }

    @PutMapping("/serve/{queueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public void markAsServed(@PathVariable Long queueId) {
        queueService.markAsServed(queueId);
    }
}
