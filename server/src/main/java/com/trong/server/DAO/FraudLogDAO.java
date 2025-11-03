package com.trong.server.DAO;

import com.trong.model.FraudLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FraudLogDAO extends DAO {

    public int insertFraudLog(FraudLog fraudLog) {
        String sql = "INSERT INTO tblFraudLog (startTimeSeconds, endTimeSeconds, behaviorType, " +
                "conclusionStatus, concluderDate, finalVerdict, tblProcessingLogid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, fraudLog.getStartTimeSeconds());
            ps.setDouble(2, fraudLog.getEndTimeSeconds());
            ps.setString(3, fraudLog.getBehaviorType());
            ps.setString(4, fraudLog.getConclusionStatus());
            if (fraudLog.getConcluderDate() != null) {
                ps.setTimestamp(5, new java.sql.Timestamp(fraudLog.getConcluderDate().getTime()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            if (fraudLog.getFinalVerdict() != null) {
                ps.setString(6, fraudLog.getFinalVerdict());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }
            ps.setInt(7, fraudLog.getProcessingLogId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<FraudLog> getFraudLogsByProcessingLogId(int processingLogId) {
        List<FraudLog> list = new ArrayList<>();
        String sql = "SELECT * FROM tblFraudLog WHERE tblProcessingLogid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, processingLogId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                FraudLog fl = new FraudLog();
                fl.setIdFL(rs.getInt("idFL"));
                fl.setStartTimeSeconds(rs.getDouble("startTimeSeconds"));
                fl.setEndTimeSeconds(rs.getDouble("endTimeSeconds"));
                fl.setBehaviorType(rs.getString("behaviorType"));
                fl.setConclusionStatus(rs.getString("conclusionStatus"));
                fl.setConcluderDate(rs.getTimestamp("concluderDate"));
                fl.setFinalVerdict(rs.getString("finalVerdict"));
                fl.setProcessingLogId(rs.getInt("tblProcessingLogid"));
                list.add(fl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public FraudLog getFraudLogById(int idFL) {
        String sql = "SELECT * FROM tblFraudLog WHERE idFL = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFL);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                FraudLog fl = new FraudLog();
                fl.setIdFL(rs.getInt("idFL"));
                fl.setStartTimeSeconds(rs.getDouble("startTimeSeconds"));
                fl.setEndTimeSeconds(rs.getDouble("endTimeSeconds"));
                fl.setBehaviorType(rs.getString("behaviorType"));
                fl.setConclusionStatus(rs.getString("conclusionStatus"));
                fl.setConcluderDate(rs.getTimestamp("concluderDate"));
                fl.setFinalVerdict(rs.getString("finalVerdict"));
                fl.setProcessingLogId(rs.getInt("tblProcessingLogid"));
                return fl;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean editFraudLog(FraudLog log) {
        String sql = "UPDATE tblFraudLog SET startTimeSeconds = ?, endTimeSeconds = ?, " +
                "behaviorType = ?, conclusionStatus = ?, finalVerdict = ?, concluderDate = ? " +
                "WHERE idFL = ?";

        boolean oldAutoCommit = false;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setDouble(1, log.getStartTimeSeconds());
                ps.setDouble(2, log.getEndTimeSeconds());
                ps.setString(3, log.getBehaviorType());
                ps.setString(4, log.getConclusionStatus());

                if (log.getFinalVerdict() != null) {
                    ps.setString(5, log.getFinalVerdict());
                } else {
                    ps.setNull(5, Types.VARCHAR);
                }
                if (log.getConcluderDate() != null) {
                    ps.setTimestamp(6, new Timestamp(log.getConcluderDate().getTime()));
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }

                ps.setInt(7, log.getIdFL());

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

    public boolean editFraudLogBatch(List<FraudLog> logs) {
        String sql = "UPDATE tblFraudLog SET conclusionStatus = ?, concluderDate = ?, finalVerdict = ? " +
                "WHERE idFL = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (FraudLog log : logs) {
                ps.setString(1, log.getConclusionStatus());

                if (log.getConcluderDate() != null) {
                    ps.setTimestamp(2, new Timestamp(log.getConcluderDate().getTime()));
                } else {
                    ps.setNull(2, Types.TIMESTAMP);
                }

                if (log.getFinalVerdict() != null) {
                    ps.setString(3, log.getFinalVerdict());
                } else {
                    ps.setNull(3, Types.VARCHAR);
                }

                ps.setInt(4, log.getIdFL());

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

