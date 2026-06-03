package com.dfems.dao;

import com.dfems.db.DBConnection;
import com.dfems.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String email) throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.email, u.role_id, u.dept_id, " +
                     "r.role_name, d.dept_name " +
                     "FROM USERS u " +
                     "JOIN ROLE r ON u.role_id = r.role_id " +
                     "JOIN DEPARTMENT d ON u.dept_id = d.dept_id " +
                     "WHERE u.email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setRoleId(rs.getInt("role_id"));
                u.setDeptId(rs.getInt("dept_id"));
                u.setRoleName(rs.getString("role_name"));
                u.setDeptName(rs.getString("dept_name"));
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.name, u.email, r.role_name, d.dept_name " +
                     "FROM USERS u " +
                     "JOIN ROLE r ON u.role_id = r.role_id " +
                     "JOIN DEPARTMENT d ON u.dept_id = d.dept_id " +
                     "ORDER BY u.name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setRoleName(rs.getString("role_name"));
                u.setDeptName(rs.getString("dept_name"));
                list.add(u);
            }
        }
        return list;
    }

    public void logAction(int userId, String action, String details) throws SQLException {
        String sql = "{call pkg_dfems.log_user_action(?,?,?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, userId);
            cs.setString(2, action);
            cs.setString(3, details);
            cs.execute();
        }
    }
}
