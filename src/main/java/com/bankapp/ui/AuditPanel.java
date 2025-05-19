package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder panel for audit-log viewing (auditor/manager/dba).
 */
public class AuditPanel extends JPanel {
    public AuditPanel(java.sql.Connection conn) {
        setLayout(new BorderLayout());
        add(new JLabel("Audit log viewer coming soon", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
