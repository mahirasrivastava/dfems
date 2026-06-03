package com.dfems.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ── CHANGE THESE TO MATCH YOUR ORACLE SETUP ──────────────
    private static final String URL      = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USERNAME = "dfems_user";
    private static final String PASSWORD = "dfems_pass";
    // ─────────────────────────────────────────────────────────

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                connection.setAutoCommit(false);
                System.out.println("[DB] Connection established.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC Driver not found. Add ojdbc11.jar to classpath.", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
