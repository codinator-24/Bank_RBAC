package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder panel for role administration (manager/dba).
 */
public class RoleAdminPanel extends JPanel {
    public RoleAdminPanel(java.sql.Connection conn) {
        setLayout(new BorderLayout());
        add(new JLabel("Role administration coming soon", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
