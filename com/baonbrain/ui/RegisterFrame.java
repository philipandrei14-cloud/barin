package com.baonbrain.ui;

import com.baonbrain.model.User;
import com.baonbrain.util.AppColors;
import com.baonbrain.util.DataStore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class RegisterFrame extends JDialog {

    private JTextField fullNameField, usernameField, emailField;
    private JPasswordField passwordField, confirmField;
    private JLabel statusLabel;

    public RegisterFrame(JFrame parent) {
        super(parent, "BaonBrain - Register", true);
        setSize(420, 540);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG_MAIN);

        JPanel header = new JPanel();
        header.setBackground(AppColors.PRIMARY_LIGHT);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Create Your Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title);

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 40, 20, 40));

        form.add(makeLabel("Full Name"));
        form.add(Box.createVerticalStrut(4));
        fullNameField = makeTextField();
        form.add(fullNameField);
        form.add(Box.createVerticalStrut(10));

        form.add(makeLabel("Username"));
        form.add(Box.createVerticalStrut(4));
        usernameField = makeTextField();
        form.add(usernameField);
        form.add(Box.createVerticalStrut(10));

        form.add(makeLabel("Email"));
        form.add(Box.createVerticalStrut(4));
        emailField = makeTextField();
        form.add(emailField);
        form.add(Box.createVerticalStrut(10));

        form.add(makeLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        passwordField = new JPasswordField();
        styleField(passwordField);
        form.add(passwordField);
        form.add(Box.createVerticalStrut(10));

        form.add(makeLabel("Confirm Password"));
        form.add(Box.createVerticalStrut(4));
        confirmField = new JPasswordField();
        styleField(confirmField);
        form.add(confirmField);
        form.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(AppColors.DANGER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(12));

        JButton registerBtn = makeButton("Register", AppColors.ACCENT);
        registerBtn.addActionListener(e -> doRegister());
        form.add(registerBtn);

        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        setContentPane(root);
    }

    private void doRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Full name, username, and password are required."); return;
        }
        if (!pass.equals(confirm)) {
            statusLabel.setText("Passwords do not match."); return;
        }
        if (pass.length() < 4) {
            statusLabel.setText("Password must be at least 4 characters."); return;
        }
        if (DataStore.usernameExists(username)) {
            statusLabel.setText("Username already taken."); return;
        }

        User user = new User(0, username, pass, fullName, email);
        DataStore.saveUser(user);
        JOptionPane.showMessageDialog(this, "Account created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(AppColors.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeTextField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
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