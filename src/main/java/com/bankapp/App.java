package com.bankapp;

import com.bankapp.ui.LoginDialog;
import com.bankapp.ui.MainFrame;

import javax.swing.*;
import java.sql.Connection;

/**
 * Application entry point.
 * Shows LoginDialog, then launches MainFrame on success.
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1) Show login dialog
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            // 2) If login failed or cancelled, exit
            if (!login.isSucceeded()) {
                System.exit(0);
            }

            // 3) On success, retrieve connection and personId
            Connection conn = login.getConnection();
            int personId = login.getPersonId();

            // 4) Launch MainFrame
            MainFrame mainFrame = new MainFrame(conn, personId);
            mainFrame.setVisible(true);
        });
    }
}
