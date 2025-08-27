package com.qrqueue.backend.payload;

public class JoinRequest {

    private String customerName;
    private Long counterId;

    public JoinRequest() {
    }

    public JoinRequest(String customerName, Long counterId) {
        this.customerName = customerName;
        this.counterId = counterId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }
}
