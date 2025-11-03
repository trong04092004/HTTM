package com.trong.server.DAO;

import com.trong.model.ProcessingLog;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ProcessingLogDAO extends DAO {

    public int insertProcessingLog(ProcessingLog log) {
        String sql = "INSERT INTO tblProcessingLog (startTime, endTime, status, outputPath, tblVideoid) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, new java.sql.Date(log.getStartTime().getTime()));
            ps.setDate(2, new java.sql.Date(log.getEndTime().getTime()));
            ps.setString(3, log.getStatus());
            ps.setString(4, log.getOutputPath());
            ps.setInt(5, log.getVideoId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Trả về idPL vừa thêm
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ProcessingLog getProcessingLogById(int idPL) {
        // (Code của bạn giữ nguyên)
        String sql = "SELECT * FROM tblProcessingLog WHERE idPL = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPL);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProcessingLog log = new ProcessingLog();
                log.setIdPL(rs.getInt("idPL"));
                log.setStartTime(rs.getDate("startTime"));
                log.setEndTime(rs.getDate("endTime"));
                log.setStatus(rs.getString("status"));
                log.setOutputPath(rs.getString("outputPath"));
                log.setVideoId(rs.getInt("tblVideoid"));
                return log;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> getLogAndVideoDetails(int processingLogId) {
        Map<String, Object> details = new HashMap<>();

        String sql = "SELECT p.status, p.outputPath, v.url AS videoUrl " +
                "FROM tblProcessingLog p " +
                "JOIN tblVideo v ON p.tblVideoid = v.idVideo " +
                "WHERE p.idPL = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, processingLogId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                details.put("status", rs.getString("status"));
                details.put("outputPath", rs.getString("outputPath"));
                details.put("videoUrl", rs.getString("videoUrl"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
    public boolean updateStatus(int processingLogId, String status) {
        String sql = "UPDATE tblProcessingLog SET status = ? WHERE idPL = ?";
        boolean oldAutoCommit = false;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, processingLogId);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit(); // <-- Lưu thay đổi
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { connection.setAutoCommit(oldAutoCommit); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}

