package com.bankapp.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Panel for DBAs to create and drop MySQL users.
 */
public class UserAdminPanel extends JPanel {
    private final Connection conn;
    private final DefaultTableModel userModel;
    private final JTable userTable;
    private final JTextField txtNewUser;
    private final JPasswordField txtNewPass;

    public UserAdminPanel(Connection connection) {
        super(new BorderLayout());
        this.conn = connection;

        // 1) User list table
        String[] cols = {"User", "Host"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        userTable = new JTable(userModel);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        loadUsers();

        // 2) Controls for create/drop
        JPanel ctrl = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(2, 2, 2, 2);
        cs.fill = GridBagConstraints.HORIZONTAL;

        // New username
        cs.gridx = 0; cs.gridy = 0;
        ctrl.add(new JLabel("New User:"), cs);
        txtNewUser = new JTextField(10);
        cs.gridx = 1;
        ctrl.add(txtNewUser, cs);

        // New password
        cs.gridx = 0; cs.gridy = 1;
        ctrl.add(new JLabel("Password:"), cs);
        txtNewPass = new JPasswordField(10);
        cs.gridx = 1;
        ctrl.add(txtNewPass, cs);

        // Create button
        JButton btnCreate = new JButton("Create User");
        btnCreate.addActionListener(e -> createUser());
        cs.gridx = 0; cs.gridy = 2; cs.gridwidth = 2;
        ctrl.add(btnCreate, cs);

        // Drop button
        JButton btnDrop = new JButton("Drop Selected");
        btnDrop.addActionListener(e -> dropSelectedUser());
        cs.gridy = 3;
        ctrl.add(btnDrop, cs);

        add(ctrl, BorderLayout.SOUTH);
    }

    /** Load all MySQL users (User, Host) into the table. */
    private void loadUsers() {
        userModel.setRowCount(0);
        String sql = "SELECT User, Host FROM mysql.user ORDER BY User,Host";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                userModel.addRow(new Object[]{
                    rs.getString("User"),
                    rs.getString("Host")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load users: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Creates a new MySQL user as entered in the form. */
/** Creates a new MySQL user as entered in the form and grants bank privileges. */
private void createUser() {
    String u = txtNewUser.getText().trim();
    String p = new String(txtNewPass.getPassword());
    if (u.isEmpty() || p.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Enter both username and password");
        return;
    }
    try (Statement st = conn.createStatement()) {
        // 1) Create the MySQL account
        st.execute(String.format(
            "CREATE USER '%s'@'localhost' IDENTIFIED BY '%s'", u, p));

        // 2) Grant ALL privileges on bank.* 
        st.execute(String.format(
            "GRANT ALL ON bank.* TO '%s'@'localhost'", u));

        // 3) Inform the admin
        JOptionPane.showMessageDialog(this,
            "User created: " + u +
            "\nGranted ALL privileges on bank.*" +
            "\n(Remember to insert this user into your Person table for role assignments.)");

        // 4) Refresh the user list
        loadUsers();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error creating user: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    /** Drops the selected user from the table. */
    private void dropSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Select a user to drop");
            return;
        }
        String u = (String) userModel.getValueAt(row, 0);
        String h = (String) userModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Drop user '" + u + "'@'" + h + "'?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = String.format(
            "DROP USER '%s'@'%s'", u, h);
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
            JOptionPane.showMessageDialog(this,
                "User dropped: " + u);
            loadUsers();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error dropping user: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
