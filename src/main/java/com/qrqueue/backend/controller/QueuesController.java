package com.qrqueue.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qrqueue.backend.model.QueueEntry;
import com.qrqueue.backend.repository.QueueEntryRepository;

@RestController
@RequestMapping("/queues")
public class QueuesController {
    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @GetMapping("/all")
    public List<QueueEntry> getAllQueues() {
        return queueEntryRepository.findAll();
    }
}
