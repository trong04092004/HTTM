package com.trong.model;

import java.util.Date;
import java.util.List;

public class ProcessingLog {

    private int idPL;
    private Date startTime;
    private Date endTime;
    private String status;
    private String outputPath;
    private int videoId;
    private List<FraudLog> fraudLog;

    public ProcessingLog() {}

    public ProcessingLog(int idPL, Date startTime, Date endTime, String status, String outputPath, int videoId, List<FraudLog> fraudLog) {
        this.idPL = idPL;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.outputPath = outputPath;
        this.videoId = videoId;
        this.fraudLog = fraudLog;
    }

    public int getIdPL() {
        return idPL;
    }

    public void setIdPL(int idPL) {
        this.idPL = idPL;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public List<FraudLog> getFraudLog() {
        return fraudLog;
    }

    public void setFraudLog(List<FraudLog> fraudLog) {
        this.fraudLog = fraudLog;
    }
}
