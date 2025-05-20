package com.bankapp.ui;

import com.bankapp.util.DbUtil;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;

/**
 * Panel to view and filter audit-log entries.
 */
public class AuditPanel extends JPanel {
    private final Connection conn;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JTextField txtPersonId;
    private final JTextField txtRoleId;
    private final JFormattedTextField txtFromDate;
    private final JFormattedTextField txtToDate;

    public AuditPanel(Connection connection) {
        this.conn = connection;
        setLayout(new BorderLayout());

        // Table setup
        String[] cols = {
            "AuditID","PersonID","RoleID","ObjectName",
            "Action","Timestamp","Success"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load data
        loadAuditData();

        // Filters panel
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("PersonID:"));
        txtPersonId = new JTextField(5);
        filters.add(txtPersonId);

        filters.add(new JLabel("RoleID:"));
        txtRoleId = new JTextField(5);
        filters.add(txtRoleId);

        filters.add(new JLabel("From (yyyy-MM-dd):"));
        txtFromDate = new JFormattedTextField(
            new SimpleDateFormat("yyyy-MM-dd")
        );
        txtFromDate.setColumns(8);
        filters.add(txtFromDate);

        filters.add(new JLabel("To (yyyy-MM-dd):"));
        txtToDate = new JFormattedTextField(
            new SimpleDateFormat("yyyy-MM-dd")
        );
        txtToDate.setColumns(8);
        filters.add(txtToDate);

        JButton btnApply = new JButton("Apply Filters");
        btnApply.addActionListener(e -> applyFilters());
        filters.add(btnApply);

        add(filters, BorderLayout.NORTH);
    }

    private void loadAuditData() {
        model.setRowCount(0);
        String sql = 
          "SELECT AuditID, PersonID, RoleID, ObjectName, Action, Timestamp, SuccessFlag " +
          "FROM Audit_Log";
        try (
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)
        ) {
            while (rs.next()) {
                Object[] row = new Object[] {
                    rs.getInt("AuditID"),
                    rs.getInt("PersonID"),
                    rs.getInt("RoleID"),
                    rs.getString("ObjectName"),
                    rs.getString("Action"),
                    rs.getTimestamp("Timestamp"),
                    rs.getBoolean("SuccessFlag")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to load audit logs: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void applyFilters() {
        List<RowFilter<? super DefaultTableModel, ? super Integer>> filters =
            new ArrayList<>();

        // PersonID filter
        String pid = txtPersonId.getText().trim();
        if (!pid.isEmpty()) {
            try {
                int id = Integer.parseInt(pid);
                filters.add(
                  RowFilter.numberFilter(ComparisonType.EQUAL, id, 1)
                );
            } catch (NumberFormatException ignored) {}
        }

        // RoleID filter
        String rid = txtRoleId.getText().trim();
        if (!rid.isEmpty()) {
            try {
                int id = Integer.parseInt(rid);
                filters.add(
                  RowFilter.numberFilter(ComparisonType.EQUAL, id, 2)
                );
            } catch (NumberFormatException ignored) {}
        }

        // Date range filter
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String from = txtFromDate.getText().trim();
            if (!from.isEmpty()) {
                Date d1 = fmt.parse(from);
                filters.add(
                  RowFilter.dateFilter(ComparisonType.AFTER, d1, 5)
                );
            }
        } catch (Exception ignored) {}
        try {
            String to = txtToDate.getText().trim();
            if (!to.isEmpty()) {
                Date d2 = fmt.parse(to);
                Calendar c = Calendar.getInstance();
                c.setTime(d2);
                c.add(Calendar.DATE, 1);
                filters.add(
                  RowFilter.dateFilter(ComparisonType.BEFORE, c.getTime(), 5)
                );
            }
        } catch (Exception ignored) {}

        // Combine
        RowFilter<DefaultTableModel, Integer> combined =
          (RowFilter<DefaultTableModel, Integer>)
            RowFilter.andFilter((List) filters);
        sorter.setRowFilter(combined);
    }
}