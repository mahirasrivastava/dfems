package com.dfems.dao;

import com.dfems.db.DBConnection;
import com.dfems.model.Analysis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnalysisDAO {

    public int createAnalysis(Analysis a) throws SQLException {
        String sql = "{call sp_create_analysis(?,?,?,?,?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, a.getEvidenceId());
            cs.setInt(2, a.getAnalystId());
            cs.setInt(3, a.getToolId());
            cs.setString(4, a.getFindings());
            cs.registerOutParameter(5, Types.NUMERIC);
            cs.execute();
            return cs.getInt(5);
        }
    }

    public List<Analysis> getAllAnalysis() throws SQLException {
        List<Analysis> list = new ArrayList<>();
        String sql = "SELECT ar.analysis_id, ar.evidence_id, ar.analyst_id, ar.tool_id, " +
                     "ar.findings, TO_CHAR(ar.analysis_date,'YYYY-MM-DD') AS analysis_date_str, " +
                     "ar.status, u.name AS analyst_name, ft.tool_name, " +
                     "e.file_name AS evidence_file_name, c.case_number, c.case_id " +
                     "FROM ANALYSIS_RESULTS ar " +
                     "JOIN USERS u ON ar.analyst_id = u.user_id " +
                     "JOIN FORENSIC_TOOL ft ON ar.tool_id = ft.tool_id " +
                     "JOIN EVIDENCE e ON ar.evidence_id = e.evidence_id " +
                     "JOIN CASES c ON e.case_id = c.case_id " +
                     "ORDER BY ar.analysis_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Analysis> getAnalysisByCase(int caseId) throws SQLException {
        List<Analysis> list = new ArrayList<>();
        String sql = "SELECT ar.analysis_id, ar.evidence_id, ar.analyst_id, ar.tool_id, " +
                     "ar.findings, TO_CHAR(ar.analysis_date,'YYYY-MM-DD') AS analysis_date_str, " +
                     "ar.status, u.name AS analyst_name, ft.tool_name, " +
                     "e.file_name AS evidence_file_name, c.case_number, c.case_id " +
                     "FROM ANALYSIS_RESULTS ar " +
                     "JOIN USERS u ON ar.analyst_id = u.user_id " +
                     "JOIN FORENSIC_TOOL ft ON ar.tool_id = ft.tool_id " +
                     "JOIN EVIDENCE e ON ar.evidence_id = e.evidence_id " +
                     "JOIN CASES c ON e.case_id = c.case_id " +
                     "WHERE c.case_id = ? ORDER BY ar.analysis_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Analysis> getAnalysisByStatus(String status) throws SQLException {
        List<Analysis> list = new ArrayList<>();
        String sql = "SELECT ar.analysis_id, ar.evidence_id, ar.analyst_id, ar.tool_id, " +
                     "ar.findings, TO_CHAR(ar.analysis_date,'YYYY-MM-DD') AS analysis_date_str, " +
                     "ar.status, u.name AS analyst_name, ft.tool_name, " +
                     "e.file_name AS evidence_file_name, c.case_number, c.case_id " +
                     "FROM ANALYSIS_RESULTS ar " +
                     "JOIN USERS u ON ar.analyst_id = u.user_id " +
                     "JOIN FORENSIC_TOOL ft ON ar.tool_id = ft.tool_id " +
                     "JOIN EVIDENCE e ON ar.evidence_id = e.evidence_id " +
                     "JOIN CASES c ON e.case_id = c.case_id " +
                     "WHERE ar.status = ? ORDER BY ar.analysis_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void updateStatus(int analysisId, String newStatus) throws SQLException {
        String sql = "UPDATE ANALYSIS_RESULTS SET status = ? WHERE analysis_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, analysisId);
            ps.executeUpdate();
            conn.commit();
        }
    }

    public void deleteAnalysis(int analysisId) throws SQLException {
        String sql = "DELETE FROM ANALYSIS_RESULTS WHERE analysis_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, analysisId);
            ps.executeUpdate();
            conn.commit();
        }
    }

    private Analysis mapRow(ResultSet rs) throws SQLException {
        Analysis a = new Analysis();
        a.setAnalysisId(rs.getInt("analysis_id"));
        a.setEvidenceId(rs.getInt("evidence_id"));
        a.setAnalystId(rs.getInt("analyst_id"));
        a.setToolId(rs.getInt("tool_id"));
        a.setFindings(rs.getString("findings"));
        a.setAnalysisDateStr(rs.getString("analysis_date_str"));
        a.setStatus(rs.getString("status"));
        a.setAnalystName(rs.getString("analyst_name"));
        a.setToolName(rs.getString("tool_name"));
        a.setEvidenceFileName(rs.getString("evidence_file_name"));
        a.setCaseNumber(rs.getString("case_number"));
        a.setCaseId(rs.getInt("case_id"));
        return a;
    }
}
