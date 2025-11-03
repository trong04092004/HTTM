package com.trong.model;

import java.util.Date;

public class ConclusionHistory {

    private int idCH;
    private String oldStatus;
    private String newStatus;
    private Date conclusionTimestamp;
    private String verdictChanges;
    private User user; // Quan hệ N-1 với User
    private FraudLog fraudLog; // Quan hệ N-1 với FraudLog

    // Constructors
    public ConclusionHistory() {
    }

    public ConclusionHistory(int idCH, String oldStatus, String newStatus, Date conclusionTimestamp, String verdictChanges, User user, FraudLog fraudLog) {
        this.idCH = idCH;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.conclusionTimestamp = conclusionTimestamp;
        this.verdictChanges = verdictChanges;
        this.user = user;
        this.fraudLog = fraudLog;
    }

    // Getters and Setters
    public int getIdCH() {
        return idCH;
    }

    public void setIdCH(int idCH) {
        this.idCH = idCH;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Date getConclusionTimestamp() {
        return conclusionTimestamp;
    }

    public void setConclusionTimestamp(Date conclusionTimestamp) {
        this.conclusionTimestamp = conclusionTimestamp;
    }

    public String getVerdictChanges() {
        return verdictChanges;
    }

    public void setVerdictChanges(String verdictChanges) {
        this.verdictChanges = verdictChanges;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FraudLog getFraudLog() {
        return fraudLog;
    }

    public void setFraudLog(FraudLog fraudLog) {
        this.fraudLog = fraudLog;
    }
}
