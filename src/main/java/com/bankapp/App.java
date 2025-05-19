package com.bankapp;

import javax.swing.*;
import java.awt.*;

public class App {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("RBAC GUI");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      // Use BorderLayout explicitly
      frame.getContentPane().setLayout(new BorderLayout());

      JLabel label = new JLabel("Hello, RBAC World!", SwingConstants.CENTER);
      frame.getContentPane().add(label, BorderLayout.CENTER);

      // Pack before making visible
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
