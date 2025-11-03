package com.trong.model;

import java.util.Date;

public class FraudLog {

    private int idFL;
    private double startTimeSeconds; // Sửa từ Date startTimestamp
    private double endTimeSeconds;   // Sửa từ Date endTimestamp
    private String behaviorType;
    private String conclusionStatus;
    private Date concluderDate;
    private String finalVerdict;
    private int processingLogId;

    public FraudLog() {}
    public FraudLog(int idFL, double startTimeSeconds, double endTimeSeconds, String behaviorType,
                    String conclusionStatus, Date concluderDate, String finalVerdict, int processingLogId) {
        this.idFL = idFL;
        this.startTimeSeconds = startTimeSeconds;
        this.endTimeSeconds = endTimeSeconds;
        this.behaviorType = behaviorType;
        this.conclusionStatus = conclusionStatus;
        this.concluderDate = concluderDate;
        this.finalVerdict = finalVerdict;
        this.processingLogId = processingLogId;
    }

    // === SỬA ĐỔI GETTERS & SETTERS ===
    public int getIdFL() {
        return idFL;
    }

    public void setIdFL(int idFL) {
        this.idFL = idFL;
    }

    public double getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(double startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public double getEndTimeSeconds() {
        return endTimeSeconds;
    }

    public void setEndTimeSeconds(double endTimeSeconds) {
        this.endTimeSeconds = endTimeSeconds;
    }

    // (Các getters/setters còn lại giữ nguyên)
    public String getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(String behaviorType) {
        this.behaviorType = behaviorType;
    }

    public String getConclusionStatus() {
        return conclusionStatus;
    }

    public void setConclusionStatus(String conclusionStatus) {
        this.conclusionStatus = conclusionStatus;
    }

    public Date getConcluderDate() {
        return concluderDate;
    }

    public void setConcluderDate(Date concluderDate) {
        this.concluderDate = concluderDate;
    }

    public String getFinalVerdict() {
        return finalVerdict;
    }

    public void setFinalVerdict(String finalVerdict) {
        this.finalVerdict = finalVerdict;
    }

    public int getProcessingLogId() {
        return processingLogId;
    }

    public void setProcessingLogId(int processingLogId) {
        this.processingLogId = processingLogId;
    }
}