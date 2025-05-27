package com.bankapp.ui;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.sql.SQLException;


public class MainFrame extends JFrame {
    private final Connection conn;
    private final int personId;
    private final Set<String> roles = new HashSet<>();

    public MainFrame(Connection connection, int personId) {
        super("RBAC Banking System");
        this.conn = connection;
        this.personId = personId;

        loadUserRoles();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // Teller and Manager both get Accounts
        if (roles.contains("teller") || roles.contains("manager")) {
            tabs.addTab("Accounts", new AccountPanel(conn));
        }

        // Only DBA gets RoleAdmin
        if (roles.contains("dba")) {
            tabs.addTab("User Admin", new UserAdminPanel(conn));
            tabs.addTab("Role Admin", new RoleAdminPanel(conn));
        }

        // Auditor, Manager, and DBA get Audit Log
        if (roles.contains("auditor") || roles.contains("manager") || roles.contains("dba")) {
            tabs.addTab("Audit Log", new AuditPanel(conn));
        }

        getContentPane().add(tabs);

        // — Build Menu Bar —
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        fileMenu.add(logoutItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Logout handler
        logoutItem.addActionListener(e -> {
            try {
                conn.close();
            } catch (SQLException ignored) {}
            dispose();                // close main window
            // re-open login dialog
            SwingUtilities.invokeLater(() -> {
                LoginDialog login = new LoginDialog(null);
                login.setVisible(true);
                if (login.isSucceeded()) {
                    MainFrame newMain = new MainFrame(
                        login.getConnection(), login.getPersonId()
                    );
                    newMain.setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        });

    }

    private void loadUserRoles() {
        String sql = "SELECT r.RoleName "
                   + "FROM UserRole ur JOIN Role r ON ur.RoleID = r.RoleID "
                   + "WHERE ur.PersonID = ?";
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