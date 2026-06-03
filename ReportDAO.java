package com.dfems.dao;

import com.dfems.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public int generateReport(int caseId, int createdBy) throws SQLException {
        String sql = "{call sp_generate_report(?,?,?,?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, caseId);
            cs.setInt(2, createdBy);
            cs.setString(3, "FINAL");
            cs.registerOutParameter(4, Types.NUMERIC);
            cs.execute();
            return cs.getInt(4);
        }
    }

    public String getReportText(int reportId) throws SQLException {
        String sql = "SELECT report_text FROM FORENSIC_REPORT WHERE report_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        }
        return "";
    }

    public List<String[]> getAllReports() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT fr.report_id, c.case_number, u.name AS created_by, " +
                     "TO_CHAR(fr.created_date,'YYYY-MM-DD') AS created_date, fr.report_type " +
                     "FROM FORENSIC_REPORT fr " +
                     "JOIN CASES c ON fr.case_id = c.case_id " +
                     "JOIN USERS u ON fr.created_by = u.user_id " +
                     "ORDER BY fr.created_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("report_id")),
                    rs.getString("case_number"),
                    rs.getString("created_by"),
                    rs.getString("created_date"),
                    rs.getString("report_type")
                });
            }
        }
        return list;
    }
}
