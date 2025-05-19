package com.bankapp.ui;

import com.bankapp.util.DbUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class MainFrame extends JFrame {
    private final Connection conn;
    private final int personId;
    private final Set<String> roles = new HashSet<>();

    public MainFrame(Connection connection, int personId) {
        super("RBAC Banking System");
        this.conn = connection;
        this.personId = personId;

        // 1) Load roles from DB
        loadUserRoles();

        // 2) Setup frame
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // 3) Build UI
        JTabbedPane tabs = new JTabbedPane();

        // Account operations for Teller & Manager
        if (roles.contains("teller") || roles.contains("manager")) {
            tabs.addTab("Accounts", new AccountPanel(conn));
        }

        // Role management for Manager & DBA
        if (roles.contains("manager") || roles.contains("dba")) {
            tabs.addTab("Role Admin", new RoleAdminPanel(conn));
        }

        // Audit viewer for Auditor, Manager & DBA
        if (roles.contains("auditor") || roles.contains("manager") || roles.contains("dba")) {
            tabs.addTab("Audit Log", new AuditPanel(conn));
        }

        getContentPane().add(tabs);
    }

    private void loadUserRoles() {
        String sql = "SELECT r.RoleName " +
                     "FROM UserRole ur JOIN Role r ON ur.RoleID = r.RoleID " +
                     "WHERE ur.PersonID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("RoleName"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load user roles: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
