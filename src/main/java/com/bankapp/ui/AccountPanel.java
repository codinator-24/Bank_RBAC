package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder panel for account operations (teller/manager).
 */
public class AccountPanel extends JPanel {
    public AccountPanel(java.sql.Connection conn) {
        setLayout(new BorderLayout());
        add(new JLabel("Account operations coming soon", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
