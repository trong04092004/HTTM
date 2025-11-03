package com.trong.model;

import java.util.Date;
import java.util.List;

public class Video {

    private int idVideo;
    private int duration;
    private String url;
    private Date uploadDate;
    private User user;
    private List<ProcessingLog> process;

    public Video() {
    }

    public Video(int idVideo, int duration, String url, Date uploadDate, User user, List<ProcessingLog> process) {
        this.idVideo = idVideo;
        this.duration = duration;
        this.url = url;
        this.uploadDate = uploadDate;
        this.user = user;
        this.process = process;
    }

    // Getters and Setters
    public int getIdVideo() {
        return idVideo;
    }

    public void setIdVideo(int idVideo) {
        this.idVideo = idVideo;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ProcessingLog> getProcess() {
        return process;
    }

    public void setProcess(List<ProcessingLog> process) {
        this.process = process;
    }
}