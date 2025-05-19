package com.bankapp;

import com.bankapp.ui.LoginDialog;

import javax.swing.*;

/**
 * Entry point for the RBAC GUI application.
 * Currently only shows the LoginDialog.
 * Later, MainFrame will be launched after successful login.
 */
public class App {
    public static void main(String[] args) {

     try {
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName()
        );
    } catch (Exception e) {
        // If it fails, we’ll just continue with Metal
    }      
        // Ensure Swing components are created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Display login dialog
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            // If login was not successful, exit application
            if (!login.isSucceeded()) {
                System.exit(0);
            }

            // At this point, you have a valid connection and PersonID
            // Retrieve them if needed:
            // Connection conn = login.getConnection();
            // int personId = login.getPersonId();

            // TODO: Launch MainFrame after implementation
            JOptionPane.showMessageDialog(null, "Login successful! PersonID = "
                    + login.getPersonId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            System.exit(0);
        });
    }
}
