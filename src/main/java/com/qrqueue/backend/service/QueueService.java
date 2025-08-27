package com.qrqueue.backend.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qrqueue.backend.model.Counter;
import com.qrqueue.backend.model.QueueEntry;
import com.qrqueue.backend.repository.CounterRepository;
import com.qrqueue.backend.repository.QueueEntryRepository;

@Service
public class QueueService {

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private NotificationService notificationService;

        public QueueEntry addToQueue(Long counterId, String userName) {
                Counter counter = counterRepository.findById(counterId)
                                .orElseThrow(() -> new RuntimeException("Counter not found"));

                // Check daily limit
                long todayCount = queueEntryRepository.findByCounter(counter).stream()
                                .filter(q -> q.getJoinedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                                .count();
                if (todayCount >= counter.getDailyLimit()) {
                        throw new RuntimeException("Daily limit reached");
                }

                QueueEntry entry = QueueEntry.builder()
                                .counter(counter)
                                .joinedAt(LocalDateTime.now())
                                .served(false)
                                .userName(userName)
                                .build();

                QueueEntry saved = queueEntryRepository.save(entry);

                // Broadcast to all subscribers of this counter
                notificationService.broadcastQueueUpdate(counterId);

                // Notify users whose position is now 1 or 2 (almost their turn)
                List<QueueEntry> waitingList = queueEntryRepository.findByCounter(counter).stream()
                        .filter(q -> !q.isServed())
                        .sorted(Comparator.comparing(QueueEntry::getJoinedAt))
                        .collect(Collectors.toList());
                for (int i = 0; i < waitingList.size(); i++) {
                        if (i == 0 || i == 1) {
                                notificationService.notifyUserTurn(waitingList.get(i).getId());
                        }
                }

                return saved;
        }

    public List<QueueEntry> getQueueByCounter(Long counterId) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new RuntimeException("Counter not found"));
        return queueEntryRepository.findByCounter(counter);
    }

        public QueueEntryWithPosition getQueueEntryById(Long id) {
                QueueEntry entry = queueEntryRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Queue entry not found"));
                List<QueueEntry> allForCounter = queueEntryRepository.findByCounter(entry.getCounter())
                                .stream()
                                .filter(q -> !q.isServed())
                                .sorted(Comparator.comparing(QueueEntry::getJoinedAt))
                                .collect(Collectors.toList());
                int position = -1;
                for (int i = 0; i < allForCounter.size(); i++) {
                        if (allForCounter.get(i).getId().equals(entry.getId())) {
                                position = i + 1;
                                break;
                        }
                }
                return new QueueEntryWithPosition(entry, position);
        }

        public static class QueueEntryWithPosition {
                private final QueueEntry entry;
                private final int position;

                public QueueEntryWithPosition(QueueEntry entry, int position) {
                        this.entry = entry;
                        this.position = position;
                }

                public Long getId() { return entry.getId(); }
                public Counter getCounter() { return entry.getCounter(); }
                public java.time.LocalDateTime getJoinedAt() { return entry.getJoinedAt(); }
                public boolean isServed() { return entry.isServed(); }
                public String getUserName() { return entry.getUserName(); }
                public int getPosition() { return position; }
        }

    public void markAsServed(Long queueId) {
        QueueEntry entry = queueEntryRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue entry not found"));
        entry.setServed(true);
        queueEntryRepository.save(entry);

        // Notify all clients that queue changed
        notificationService.broadcastQueueUpdate(entry.getCounter().getId());

        // Notify next user in line
        queueEntryRepository.findByCounter(entry.getCounter()).stream()
                .filter(q -> !q.isServed())
                .findFirst()
                .ifPresent(next -> notificationService.notifyUserTurn(next.getId()));
    }
}
