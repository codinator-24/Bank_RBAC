package com.bankapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DbUtil centralizes JDBC setup and RBAC role activation.
 */
public class DbUtil {

    // JDBC URL pointing to your local MySQL 'bank' database
    private static final String URL = "jdbc:mysql://localhost:3306/bank?serverTimezone=UTC";

    /**
     * Opens a new JDBC connection using the supplied credentials.
     *
     * @param username MySQL username
     * @param password MySQL password
     * @return a live Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection(String username, String password) throws SQLException {
        // Register driver (optional with modern drivers)
        // Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, username, password);
    }

    /**
     * Activates the given RBAC role for this connection session.
     *
     * @param conn     an open JDBC Connection
     * @param roleName the name of the MySQL role to activate
     * @throws SQLException if SET ROLE fails
     */
    public static void setRole(Connection conn, String roleName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Use single quotes around the role name
            stmt.execute(String.format("SET ROLE '%s'", roleName));
        }
    }

    /**
     * Closes the connection quietly.
     *
     * @param conn The Connection to close (may be null)
     */
    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); }
            catch (SQLException ignored) { /* ignore */ }
        }
    }
}
