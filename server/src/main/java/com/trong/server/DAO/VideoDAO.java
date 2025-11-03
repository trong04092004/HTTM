package com.trong.server.DAO;

import com.trong.model.Video;
import java.sql.*;

public class VideoDAO extends DAO {

    public int insertVideo(Video video) {
        String sql = "INSERT INTO tblVideo (url, uploadDate, duration, tblUserid) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, video.getUrl());
            ps.setDate(2, new java.sql.Date(video.getUploadDate().getTime()));
            ps.setInt(3, video.getDuration());
            ps.setObject(4, video.getUser() != null ? video.getUser().getIdUser() : null);

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Video getVideoById(int idVideo) {
        String sql = "SELECT * FROM tblVideo WHERE idVideo = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVideo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Video v = new Video();
                v.setIdVideo(rs.getInt("idVideo"));
                v.setUrl(rs.getString("url"));
                v.setUploadDate(rs.getDate("uploadDate"));
                v.setDuration(rs.getInt("duration"));
                return v;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
