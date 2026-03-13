package com.baonbrain.ui;

import com.baonbrain.model.User;
import com.baonbrain.util.AppColors;
import com.baonbrain.util.DataStore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    //Logo of the system
    public LoginFrame() {
        setTitle("BaonBrain | Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG_MAIN);

        //Header
        JPanel header = new JPanel();
        header.setBackground(AppColors.PRIMARY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel logo = new JLabel("💰 BaonBrain");
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Predictive Allowance System");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tagline.setForeground(new Color(174, 214, 241));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(6));
        header.add(tagline);

        // Form
        JPanel form = new JPanel();
        form.setBackground(AppColors.BG_PANEL);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel loginTitle = new JLabel("Sign In");
        loginTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        loginTitle.setForeground(AppColors.TEXT_DARK);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(loginTitle);
        form.add(Box.createVerticalStrut(20));
        form.add(makeLabel("Username"));
        form.add(Box.createVerticalStrut(4));
        usernameField = makeTextField();
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));
        form.add(makeLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        form.add(passwordField);
        form.add(Box.createVerticalStrut(6));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(AppColors.DANGER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(16));

        JButton loginBtn = makeButton("Login", AppColors.PRIMARY);
        loginBtn.addActionListener(e -> doLogin());
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(10));

        JButton registerBtn = makeButton("Create Account", AppColors.ACCENT);
        registerBtn.addActionListener(e -> openRegister());
        form.add(registerBtn);

        // Enter key support
        getRootPane().setDefaultButton(loginBtn);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        JLabel footer = new JLabel("BaonBrain   |  Group 7", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(AppColors.TEXT_MUTED);
        footer.setBorder(new EmptyBorder(10, 0, 10, 0));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }
        User found = DataStore.findUser(user, pass);
        if (found != null) {
            dispose();
            new MainFrame(found).setVisible(true);
        } else {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
        }
    }

    private void openRegister() {
        new RegisterFrame(this).setVisible(true);
    }

    //Helpers

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(AppColors.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeTextField() {
        JTextField f = new JTextField();
        styleTextField(f);
        return f;
    }

    private void styleTextField(JTextField f) {
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}