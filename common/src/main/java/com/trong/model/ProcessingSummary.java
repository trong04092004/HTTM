package com.trong.model;

public class ProcessingSummary extends ProcessingLog{

    private int totalDuration;
    private int fraudLogCount;
    private String status;
    private String summary;

    public ProcessingSummary() {
        super();
    }

    public ProcessingSummary(int totalDuration, int fraudLogCount, String status, String summary) {
        this.totalDuration = totalDuration;
        this.fraudLogCount = fraudLogCount;
        this.status = status;
        this.summary = summary;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getFraudLogCount() {
        return fraudLogCount;
    }

    public void setFraudLogCount(int fraudLogCount) {
        this.fraudLogCount = fraudLogCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}