package com.qrqueue.backend.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QueueRequest {

    @NotNull(message = "User name cannot be null")
    @Size(min = 1, max = 100, message = "User name must be between 1 and 100 characters")
    private String userName;

    @NotNull(message = "Counter ID is required")
    private Long counterId;

    // Constructors (optional, but useful)
    public QueueRequest() {
    }

    public QueueRequest(String userName, Long counterId) {
        this.userName = userName;
        this.counterId = counterId;
    }

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }
}
