package com.qrqueue.backend.dto;

import com.qrqueue.backend.model.User;

public class CounterDto {
    private Long id;
    private String name;
    private int dailyLimit;
    private int waiting;
    private User assignedStaff;

    public CounterDto(Long id, String name, int dailyLimit, int waiting, User assignedStaff) {
        this.id = id;
        this.name = name;
        this.dailyLimit = dailyLimit;
        this.waiting = waiting;
        this.assignedStaff = assignedStaff;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getDailyLimit() { return dailyLimit; }
    public int getWaiting() { return waiting; }
    public User getAssignedStaff() { return assignedStaff; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDailyLimit(int dailyLimit) { this.dailyLimit = dailyLimit; }
    public void setWaiting(int waiting) { this.waiting = waiting; }
    public void setAssignedStaff(User assignedStaff) { this.assignedStaff = assignedStaff; }
}
