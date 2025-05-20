package com.bankapp.ui;

import com.bankapp.util.DbUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Panel to display accounts and allow deposits/withdrawals.
 */
public class AccountPanel extends JPanel {
    private final Connection conn;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public AccountPanel(Connection connection) {
        this.conn = connection;
        setLayout(new BorderLayout());

        // 1) Table model with column names
        String[] cols = {"AccountID","Type","Number","Balance","Opened","Closed","Status"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 2) Load data initially
        loadAccountData();

        // 3) Controls panel
        JPanel ctrl = new JPanel();
        JButton btnDeposit = new JButton("Deposit");
        JButton btnWithdraw = new JButton("Withdraw");
        ctrl.add(btnDeposit);
        ctrl.add(btnWithdraw);
        add(ctrl, BorderLayout.SOUTH);

        // 4) Deposit handler
        btnDeposit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,"Select an account first");
                return;
            }
            int acctId = (int) tableModel.getValueAt(row,0);
            String amtStr = JOptionPane.showInputDialog(this,
                "Deposit amount:");
            if (amtStr == null) return;
            try {
                double amt = Double.parseDouble(amtStr);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Account SET CurrentBalance = CurrentBalance + ? WHERE AccountID = ?")) {
                    ps.setDouble(1, amt);
                    ps.setInt(2, acctId);
                    ps.executeUpdate();
                }
                loadAccountData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,"Invalid amount");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,"DB error: "+ex.getMessage());
            }
        });

        // 5) Withdraw handler
        btnWithdraw.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,"Select an account first");
                return;
            }
            int acctId = (int) tableModel.getValueAt(row,0);
            String amtStr = JOptionPane.showInputDialog(this,
                "Withdraw amount:");
            if (amtStr == null) return;
            try {
                double amt = Double.parseDouble(amtStr);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Account SET CurrentBalance = CurrentBalance - ? WHERE AccountID = ?")) {
                    ps.setDouble(1, amt);
                    ps.setInt(2, acctId);
                    ps.executeUpdate();
                }
                loadAccountData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,"Invalid amount");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,"DB error: "+ex.getMessage());
            }
        });
    }

    /** Reloads the tableModel from the Account table. */
    private void loadAccountData() {
        tableModel.setRowCount(0);
        String sql = "SELECT AccountID, AccountType, AccountNumber, CurrentBalance, " +
                     "DateOpened, DateClosed, AccountStatus FROM Account";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("AccountID"),
                    rs.getString("AccountType"),
                    rs.getString("AccountNumber"),
                    rs.getDouble("CurrentBalance"),
                    rs.getDate("DateOpened"),
                    rs.getDate("DateClosed"),
                    rs.getString("AccountStatus")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load accounts: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
