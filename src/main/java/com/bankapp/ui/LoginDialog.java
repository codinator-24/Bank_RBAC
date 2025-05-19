package com.bankapp.ui;

import com.bankapp.util.DbUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A modal login dialog that authenticates the user,
 * looks up their PersonID, activates their RBAC role,
 * and provides the Connection for the main application.
 */
public class LoginDialog extends JDialog {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private boolean succeeded;
    private Connection connection;
    private int personId;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;

        // Username label & text field
        cs.gridx = 0; cs.gridy = 0; cs.gridwidth = 1;
        panel.add(new JLabel("Username: "), cs);

        txtUser = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; cs.gridwidth = 2;
        panel.add(txtUser, cs);

        // Password label & field
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
            // 1) Authenticate via DbUtil
            Connection conn = DbUtil.getConnection(user, pass);

            // 2) Lookup PersonID from the Person table
            PreparedStatement ps = conn.prepareStatement(
                "SELECT PersonID FROM Person WHERE TaxIdentifier = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new SQLException("No Person found for user");
            personId = rs.getInt(1);

            // 3) Activate default role (e.g., 'teller')
            DbUtil.setRole(conn, "teller");

            connection = conn;
            succeeded = true;
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Login failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            // Reset fields
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
