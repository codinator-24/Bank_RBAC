package com.bankapp.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * Panel to manage assignment of roles to users.
 * Allows viewing current assignments, assigning new roles, and revoking roles.
 */
public class RoleAdminPanel extends JPanel {
    private final Connection conn;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> userCombo;
    private final JComboBox<String> roleCombo;

    public RoleAdminPanel(Connection connection) {
        this.conn = connection;
        setLayout(new BorderLayout());

        // Table model columns
        String[] cols = {"PersonID", "Name", "Role"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load initial assignments
        loadAssignments();

        // Controls panel
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userCombo = new JComboBox<>();
        roleCombo = new JComboBox<>();

        ctrl.add(new JLabel("User:"));
        ctrl.add(userCombo);
        ctrl.add(new JLabel("Role:"));
        ctrl.add(roleCombo);

        JButton btnAssign = new JButton("Assign Role");
        JButton btnRevoke = new JButton("Revoke Role");
        ctrl.add(btnAssign);
        ctrl.add(btnRevoke);

        add(ctrl, BorderLayout.NORTH);

        // Populate combos
        loadUsers();
        loadRoles();

        // Assign handler
        btnAssign.addActionListener(e -> {
            String user = (String) userCombo.getSelectedItem();
            String role = (String) roleCombo.getSelectedItem();
            if (user == null || role == null) {
                JOptionPane.showMessageDialog(this, "Select both user and role");
                return;
            }
            try {
                // Metadata: insert UserRole
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO UserRole (PersonID, RoleID) " +
                    "SELECT p.PersonID, r.RoleID FROM Person p, Role r " +
                    "WHERE p.FirstName=? AND r.RoleName=?"
                );
                ps.setString(1, user);
                ps.setString(2, role);
                ps.executeUpdate();

                // Native MySQL grant
                try (Statement st = conn.createStatement()) {
                    st.execute("GRANT `" + role + "` TO '" + user + "'@'localhost'");
                }

                loadAssignments();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error assigning role: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Revoke handler
        btnRevoke.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select an assignment to revoke");
                return;
            }
            int personId = (int) tableModel.getValueAt(row, 0);
            String role = (String) tableModel.getValueAt(row, 2);
            try {
                // Metadata: delete UserRole
                PreparedStatement ps = conn.prepareStatement(
                    "DELETE ur FROM UserRole ur " +
                    "JOIN Role r ON ur.RoleID=r.RoleID " +
                    "WHERE ur.PersonID=? AND r.RoleName=?"
                );
                ps.setInt(1, personId);
                ps.setString(2, role);
                ps.executeUpdate();

                // Native MySQL revoke
                try (Statement st = conn.createStatement()) {
                    st.execute("REVOKE `" + role + "` FROM '" 
                               + getUsernameByPersonId(personId) 
                               + "'@'localhost'");
                }

                loadAssignments();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error revoking role: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /** Reloads the assignment table from UserRole, Person, and Role. */
    private void loadAssignments() {
        tableModel.setRowCount(0);
        String sql = "SELECT p.PersonID, CONCAT(p.FirstName,' ',p.LastName) AS Name, r.RoleName " +
                     "FROM UserRole ur " +
                     "JOIN Person p ON ur.PersonID=p.PersonID " +
                     "JOIN Role r ON ur.RoleID=r.RoleID";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("PersonID"),
                    rs.getString("Name"),
                    rs.getString("RoleName")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load assignments: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Load all distinct first names from Person into the user combo box. */
    private void loadUsers() {
        userCombo.removeAllItems();
        String sql = "SELECT DISTINCT FirstName FROM Person";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                userCombo.addItem(rs.getString("FirstName"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load users: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Load all role names into the role combo box. */
    private void loadRoles() {
        roleCombo.removeAllItems();
        String sql = "SELECT RoleName FROM Role";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                roleCombo.addItem(rs.getString("RoleName"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load roles: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Helper to look up username by PersonID (assumes FirstName maps to MySQL user). */
    private String getUsernameByPersonId(int personId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT FirstName FROM Person WHERE PersonID = ?");
        ps.setInt(1, personId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("FirstName");
        throw new SQLException("Unknown PersonID: " + personId);
    }
}
