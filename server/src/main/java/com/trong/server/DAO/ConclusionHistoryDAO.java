package com.trong.server.DAO;

import com.trong.model.ConclusionHistory;
import com.trong.model.FraudLog;
import com.trong.model.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

public class ConclusionHistoryDAO extends DAO {


    public boolean saveConclusionHistory(ConclusionHistory history) {
        String sql = "INSERT INTO tblConclusionHistory (oldStatus, newStatus, conclusionTimestamp, " +
                "verdictChanges, tblFraudLogid, tblUserid) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        boolean oldAutoCommit = false;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, history.getOldStatus());
                ps.setString(2, history.getNewStatus());
                ps.setTimestamp(3, new Timestamp(history.getConclusionTimestamp().getTime()));
                ps.setString(4, history.getVerdictChanges());

                if (history.getFraudLog() != null) {
                    ps.setInt(5, history.getFraudLog().getIdFL());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                if (history.getUser() != null) {
                    ps.setInt(6, history.getUser().getIdUser());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Rollback khi có lỗi
            return false;
        } finally {
            try { connection.setAutoCommit(oldAutoCommit); } catch (SQLException e) { e.printStackTrace(); } // Trả về trạng thái cũ
        }
    }
    public boolean saveConclusionHistoryBatch(List<ConclusionHistory> histories) {
        String sql = "INSERT INTO tblConclusionHistory (oldStatus, newStatus, conclusionTimestamp, " +
                "verdictChanges, tblFraudLogid, tblUserid) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (ConclusionHistory history : histories) {
                ps.setString(1, history.getOldStatus());
                ps.setString(2, history.getNewStatus());
                ps.setTimestamp(3, new Timestamp(history.getConclusionTimestamp().getTime()));
                ps.setString(4, history.getVerdictChanges());
                if (history.getFraudLog() != null) {
                    ps.setInt(5, history.getFraudLog().getIdFL());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                if (history.getUser() != null) {
                    ps.setInt(6, history.getUser().getIdUser());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}

