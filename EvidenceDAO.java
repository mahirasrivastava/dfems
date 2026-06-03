package com.dfems.dao;

import com.dfems.db.DBConnection;
import com.dfems.model.Evidence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvidenceDAO {

    public int uploadEvidence(Evidence ev) throws SQLException {
        String sql = "{call sp_upload_evidence(?,?,?,?,?,?,?,?,?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, ev.getCaseId());
            cs.setInt(2, ev.getTypeId());
            cs.setInt(3, ev.getStorageId());
            cs.setString(4, ev.getFileName());
            cs.setLong(5, ev.getFileSize());
            cs.setString(6, ev.getFileType());
            cs.setString(7, ev.getHashMd5());
            cs.setInt(8, ev.getUploadedBy());
            cs.registerOutParameter(9, Types.NUMERIC);
            cs.execute();
            return cs.getInt(9);
        }
    }

    public List<Evidence> getEvidenceByCase(int caseId) throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_evidence_details WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Evidence ev = new Evidence();
                ev.setEvidenceId(rs.getInt("evidence_id"));
                ev.setFileName(rs.getString("file_name"));
                ev.setFileType(rs.getString("file_type"));
                ev.setFileSizeFormatted(rs.getString("file_size"));
                ev.setEvidenceTypeName(rs.getString("evidence_type"));
                ev.setStorageLocation(rs.getString("storage_location"));
                ev.setVerified("Y".equals(rs.getString("is_verified")));
                ev.setCaseNumber(rs.getString("case_number"));
                list.add(ev);
            }
        }
        return list;
    }

    public List<Evidence> getAllEvidence() throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_evidence_details ORDER BY upload_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Evidence ev = new Evidence();
                ev.setEvidenceId(rs.getInt("evidence_id"));
                ev.setFileName(rs.getString("file_name"));
                ev.setFileType(rs.getString("file_type"));
                ev.setFileSizeFormatted(rs.getString("file_size"));
                ev.setEvidenceTypeName(rs.getString("evidence_type"));
                ev.setStorageLocation(rs.getString("storage_location"));
                ev.setVerified("Y".equals(rs.getString("is_verified")));
                ev.setCaseNumber(rs.getString("case_number"));
                ev.setCaseId(rs.getInt("case_id"));
                list.add(ev);
            }
        }
        return list;
    }

    public void transferCustody(int evidenceId, int userId, String actionName, String remarks) throws SQLException {
        String sql = "{call sp_transfer_custody(?,?,?,?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, evidenceId);
            cs.setInt(2, userId);
            cs.setString(3, actionName);
            cs.setString(4, remarks);
            cs.execute();
        }
    }

    public List<String[]> getChainOfCustody(int evidenceId) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT handled_by, action, TO_CHAR(timestamp,'YYYY-MM-DD HH24:MI:SS') AS ts, remarks, case_number " +
                     "FROM vw_chain_of_custody WHERE evidence_id = ? ORDER BY timestamp";
        // Note: vw_chain_of_custody doesn't expose evidence_id directly, use a join query
        String sql2 = "SELECT u.name AS handled_by, at.action_name AS action, " +
                      "TO_CHAR(coc.timestamp,'YYYY-MM-DD HH24:MI:SS') AS ts, coc.remarks " +
                      "FROM CHAIN_OF_CUSTODY coc " +
                      "JOIN USERS u ON coc.user_id = u.user_id " +
                      "JOIN ACTION_TYPE at ON coc.action_id = at.action_id " +
                      "WHERE coc.evidence_id = ? ORDER BY coc.timestamp";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setInt(1, evidenceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("handled_by"),
                    rs.getString("action"),
                    rs.getString("ts"),
                    rs.getString("remarks") != null ? rs.getString("remarks") : ""
                });
            }
        }
        return list;
    }
}
