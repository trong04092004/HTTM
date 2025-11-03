package com.trong.model;

import java.util.Date;

public class VideoHistoryDTO {
    private int idVideo;
    private int idPL;
    private String tenVideo;
    private Date ngayTaiLen;
    private String trangThaiXuLy;
    private int soViPham;

    public VideoHistoryDTO() {}

    public VideoHistoryDTO(int idVideo, int idPL, String tenVideo, Date ngayTaiLen, String trangThaiXuLy, int soViPham) {
        this.idVideo = idVideo;
        this.idPL = idPL;
        this.tenVideo = tenVideo;
        this.ngayTaiLen = ngayTaiLen;
        this.trangThaiXuLy = trangThaiXuLy;
        this.soViPham = soViPham;
    }

    public int getIdVideo() { return idVideo; }
    public void setIdVideo(int idVideo) { this.idVideo = idVideo; }
    public int getIdPL() { return idPL; }
    public void setIdPL(int idPL) { this.idPL = idPL; }
    public String getTenVideo() { return tenVideo; }
    public void setTenVideo(String tenVideo) { this.tenVideo = tenVideo; }
    public Date getNgayTaiLen() { return ngayTaiLen; }
    public void setNgayTaiLen(Date ngayTaiLen) { this.ngayTaiLen = ngayTaiLen; }
    public String getTrangThaiXuLy() { return trangThaiXuLy; }
    public void setTrangThaiXuLy(String trangThaiXuLy) { this.trangThaiXuLy = trangThaiXuLy; }
    public int getSoViPham() { return soViPham; }
    public void setSoViPham(int soViPham) { this.soViPham = soViPham; }
}