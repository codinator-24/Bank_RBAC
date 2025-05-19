package com.bankapp.ui;

import com.bankapp.util.DbUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A modal login dialog that:
 * 1) Authenticates the user against MySQL
 * 2) Looks up PersonID by first name
 * 3) Fetches all RBAC roles from metadata
 * 4) Activates those roles via SET ROLE ALL
 * 5) Shows a success message, then closes
 */
public class LoginDialog extends JDialog {
    private final JTextField txtUser;
    private final JPasswordField txtPass;
    private final JButton btnLogin;
    private boolean succeeded;
    private Connection connection;
    private int personId;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);

        // Use system look and feel for native styling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        // Build form panel
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;

        // Username field
        cs.gridx = 0; cs.gridy = 0; cs.gridwidth = 1;
        panel.add(new JLabel("Username: "), cs);
        txtUser = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; cs.gridwidth = 2;
        panel.add(txtUser, cs);

        // Password field
        cs.gridx = 0; cs.gridy = 1; cs.gridwidth = 1;
        panel.add(new JLabel("Password: "), cs);
        txtPass = new JPasswordField(20);
        cs.gridx = 1; cs.gridy = 1; cs.gridwidth = 2;
        panel.add(txtPass, cs);

        // Login button
        btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> loginAction());
        JPanel bp = new JPanel();
        bp.add(btnLogin);

        // Assemble dialog
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void loginAction() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());

        try {
            // 1) Authenticate via JDBC
            Connection conn = DbUtil.getConnection(user, pass);

            // 2) Lookup PersonID by first name
            PreparedStatement ps = conn.prepareStatement(
                "SELECT PersonID FROM Person WHERE FirstName = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException("No Person found for user");
            }
            personId = rs.getInt("PersonID");

            // 3) Fetch all roles from metadata
            PreparedStatement rolePs = conn.prepareStatement(
                "SELECT r.RoleName " +
                "FROM UserRole ur JOIN Role r ON ur.RoleID = r.RoleID " +
                "WHERE ur.PersonID = ?");
            rolePs.setInt(1, personId);
            ResultSet roleRs = rolePs.executeQuery();

            List<String> userRoles = new ArrayList<>();
            while (roleRs.next()) {
                userRoles.add(roleRs.getString("RoleName"));
            }
            if (userRoles.isEmpty()) {
                throw new SQLException("No roles assigned to this user");
            }

            // 4) Activate all granted roles
            try (Statement st = conn.createStatement()) {
                // Using SET ROLE ALL to enable every granted role
                st.execute("SET ROLE ALL");
            }

            connection = conn;
            succeeded = true;

            // 5) Show success message
            JOptionPane.showMessageDialog(this,
                "Login successful! PersonID = " + personId,
                "Success", JOptionPane.INFORMATION_MESSAGE);

            // Close dialog so App can open MainFrame
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Login failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            txtPass.setText("");
            succeeded = false;
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public Connection getConnection() {
        return connection;
    }

    public int getPersonId() {
        return personId;
    }
}
