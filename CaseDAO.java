package com.dfems.dao;

import com.dfems.db.DBConnection;
import com.dfems.model.Case;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CaseDAO {

    public int registerCase(Case c) throws SQLException {
        String sql = "{call sp_register_case(?,?,?,?,?,?,?,?,?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, c.getCaseNumber());
            cs.setDate(2, new java.sql.Date(c.getCaseDate().getTime()));
            cs.setString(3, c.getDescription());
            cs.setInt(4, c.getCaseTypeId());
            cs.setString(5, c.getLocation());
            cs.setString(6, c.getArrest());
            cs.setString(7, c.getDomestic());
            cs.setInt(8, c.getCreatedBy());
            cs.registerOutParameter(9, Types.NUMERIC);
            cs.execute();
            return cs.getInt(9);
        }
    }

    public List<Case> getAllCases() throws SQLException {
        List<Case> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_case_details ORDER BY case_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Case getCaseById(int caseId) throws SQLException {
        String sql = "SELECT * FROM vw_case_details WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Case> searchCases(String keyword) throws SQLException {
        List<Case> list = new ArrayList<>();
        String sql = "{call pkg_dfems.search_cases(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, keyword);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                Case c = new Case();
                c.setCaseId(rs.getInt("case_id"));
                c.setCaseNumber(rs.getString("case_number"));
                c.setCaseTypeName(rs.getString("type_name"));
                c.setStatusName(rs.getString("status_name"));
                list.add(c);
            }
        }
        return list;
    }

    public void updateStatus(int caseId, int statusId) throws SQLException {
        String sql = "UPDATE CASES SET status_id = ? WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            ps.setInt(2, caseId);
            ps.executeUpdate();
            conn.commit();
        }
    }

    public void closeCase(int caseId, int closedBy) throws SQLException {
        String sql = "{call sp_close_case(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, caseId);
            cs.setInt(2, closedBy);
            cs.execute();
        }
    }

    public int getEvidenceCount(int caseId) throws SQLException {
        String sql = "SELECT fn_evidence_count(?) FROM DUAL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Case mapRow(ResultSet rs) throws SQLException {
        Case c = new Case();
        c.setCaseId(rs.getInt("case_id"));
        c.setCaseNumber(rs.getString("case_number"));
        c.setDescription(rs.getString("description"));
        c.setLocation(rs.getString("location"));
        try { c.setCaseTypeName(rs.getString("case_type")); }   catch (Exception ignored) {}
        try { c.setStatusName(rs.getString("case_status")); }   catch (Exception ignored) {}
        try { c.setCreatedByName(rs.getString("created_by")); } catch (Exception ignored) {}
        try { c.setEvidenceCount(rs.getInt("evidence_count")); } catch (Exception ignored) {}
        return c;
    }
}
