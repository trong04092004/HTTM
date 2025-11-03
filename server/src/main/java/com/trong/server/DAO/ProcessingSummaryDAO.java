package com.trong.server.DAO;

import com.trong.model.VideoHistoryDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProcessingSummaryDAO extends DAO {

    public List<VideoHistoryDTO> getHistorySummaryForUser(int userId) {
        List<VideoHistoryDTO> summaries = new ArrayList<>();

        String sql = "SELECT " +
                "  v.idVideo, p.idPL, v.url AS tenVideo, v.uploadDate AS ngayTaiLen, " +
                "  p.status AS trangThaiXuLy, COUNT(f.idFL) AS soViPham " +
                "FROM tblVideo v " +
                "JOIN tblProcessingLog p ON v.idVideo = p.tblVideoid " +
                "LEFT JOIN tblFraudLog f ON p.idPL = f.tblProcessingLogid " +
                "WHERE v.tblUserid = ? " +
                "GROUP BY v.idVideo, p.idPL, v.url, v.uploadDate, p.status " +
                "ORDER BY v.uploadDate DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                summaries.add(new VideoHistoryDTO(
                        rs.getInt("idVideo"),
                        rs.getInt("idPL"),
                        rs.getString("tenVideo"),
                        rs.getTimestamp("ngayTaiLen"),
                        rs.getString("trangThaiXuLy"),
                        rs.getInt("soViPham")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }
}