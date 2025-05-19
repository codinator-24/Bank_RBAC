package com.bankapp.ui;

import javax.swing.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Main application window. Shows only the tabs the user’s roles permit.
 */
public class MainFrame extends JFrame {
    private final Connection conn;
    private final int personId;
    private final Set<String> roles = new HashSet<>();

    public MainFrame(Connection connection, int personId) {
        super("RBAC Banking System");
        this.conn = connection;
        this.personId = personId;

        loadUserRoles();  // populate 'roles' set from UserRole→Role

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        if (roles.contains("teller") || roles.contains("manager")) {
            tabs.addTab("Accounts", new AccountPanel(conn));
        }
        if (roles.contains("manager") || roles.contains("dba")) {
            tabs.addTab("Role Admin", new RoleAdminPanel(conn));
        }
        if (roles.contains("auditor") || roles.contains("manager") || roles.contains("dba")) {
            tabs.addTab("Audit Log", new AuditPanel(conn));
        }

        getContentPane().add(tabs);
    }

    private void loadUserRoles() {
        String sql = "SELECT r.RoleName "
                   + "FROM UserRole ur JOIN Role r ON ur.RoleID = r.RoleID "
                   + "WHERE ur.PersonID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                roles.add(rs.getString("RoleName"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load roles: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
