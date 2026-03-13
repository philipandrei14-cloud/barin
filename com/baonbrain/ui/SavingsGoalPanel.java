package com.baonbrain.ui;

import com.baonbrain.model.*;
import com.baonbrain.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SavingsGoalPanel extends JPanel {

    private final User user;
    private final MainFrame mainFrame;
    private JPanel goalsContainer;

    public SavingsGoalPanel(User user, MainFrame mainFrame) {
        this.user = user;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        initUI();
    }
    //UI
    private void initUI() {
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(AppColors.BG_MAIN);

        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBar.setBackground(AppColors.BG_MAIN);
        titleBar.setBorder(new EmptyBorder(16, 20, 0, 20));
        JLabel title = new JLabel("🎯 Savings Goals");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleBar.add(title);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(AppColors.BG_MAIN);
        toolbar.setBorder(new EmptyBorder(0, 16, 0, 16));
        JButton addBtn = btn("+ New Goal", AppColors.ACCENT);
        addBtn.addActionListener(e -> openAddDialog());
        toolbar.add(addBtn);

        north.add(titleBar, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);

        goalsContainer = new JPanel();
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setBackground(AppColors.BG_MAIN);

        JScrollPane scroll = new JScrollPane(goalsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(north, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }
    //Notes if no savings yet
    public void refresh() {
        goalsContainer.removeAll();
        List<SavingsGoal> goals = DataStore.getGoalsByUser(user.getId());
        if (goals.isEmpty()) {
            JLabel empty = new JLabel("No savings goals yet. Click '+ New Goal' to add one.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(AppColors.TEXT_MUTED);
            empty.setBorder(new EmptyBorder(30, 30, 0, 0));
            goalsContainer.add(empty);
        }
        for (SavingsGoal goal : goals) {
            goalsContainer.add(createGoalCard(goal));
            goalsContainer.add(Box.createVerticalStrut(12));
        }
        goalsContainer.add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }
    //Creates Goal
    private JPanel createGoalCard(SavingsGoal goal) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                new EmptyBorder(14, 18, 14, 18)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JLabel name = new JLabel("🎯 " + goal.getGoalName());
        name.setFont(new Font("SansSerif", Font.BOLD, 15));
        name.setForeground(AppColors.TEXT_DARK);

        JLabel amounts = new JLabel(
                FormatUtil.currency(goal.getCurrentAmount()) + " / " +
                        FormatUtil.currency(goal.getTargetAmount()) +
                        "  |  Remaining: " + FormatUtil.currency(goal.getRemainingAmount())
        );
        amounts.setFont(new Font("SansSerif", Font.PLAIN, 12));
        amounts.setForeground(AppColors.TEXT_MUTED);

        top.add(name, BorderLayout.NORTH);
        top.add(amounts, BorderLayout.SOUTH);

        // Progress bar
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) goal.getProgressPercent());
        bar.setStringPainted(true);
        bar.setString(String.format("%.1f%%", goal.getProgressPercent()));
        bar.setForeground(goal.getProgressPercent() >= 100 ? AppColors.ACCENT : AppColors.PRIMARY_LIGHT);
        bar.setBackground(AppColors.BORDER);
        bar.setPreferredSize(new Dimension(0, 22));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setBackground(Color.WHITE);

        JButton addFund = btn("+ Add Funds", AppColors.ACCENT);
        addFund.addActionListener(e -> openAddFundsDialog(goal));
        JButton del = btn("Delete", AppColors.DANGER);
        del.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Delete this goal?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { DataStore.deleteGoal(goal.getId()); refresh(); }
        });

        actions.add(addFund);
        actions.add(del);

        if (goal.getTargetDate() != null) {
            JLabel dateLbl = new JLabel("Target: " + FormatUtil.date(goal.getTargetDate()));
            dateLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
            dateLbl.setForeground(AppColors.TEXT_MUTED);
            actions.add(dateLbl);
        }

        card.add(top, BorderLayout.NORTH);
        card.add(bar, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }
    //Add Goal
    private void openAddDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Savings Goal", true);
        d.setSize(360, 260);
        d.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 5, 6, 5);

        JTextField nameField = new JTextField();
        JTextField targetField = new JTextField();
        JTextField currentField = new JTextField("0");

        row(panel, gc, 0, "Goal Name:", nameField);
        row(panel, gc, 1, "Target Amount (₱):", targetField);
        row(panel, gc, 2, "Current Savings (₱):", currentField);

        JButton save = btn("Create Goal", AppColors.ACCENT);
        save.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new Exception("Name required.");
                double target = Double.parseDouble(targetField.getText().trim());
                double current = Double.parseDouble(currentField.getText().trim());
                SavingsGoal g = new SavingsGoal(0, user.getId(), name, target, current, null);
                DataStore.saveGoal(g);
                refresh();
                d.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Invalid number.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage());
            }
        });
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        panel.add(save, gc);
        d.add(panel);
        d.setVisible(true);
    }
    //Add Funds
    private void openAddFundsDialog(SavingsGoal goal) {
        String input = JOptionPane.showInputDialog(this,
                "Add funds to \"" + goal.getGoalName() + "\":\nCurrent: " + FormatUtil.currency(goal.getCurrentAmount()),
                "Add Funds", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            double amount = Double.parseDouble(input.trim());
            goal.setCurrentAmount(goal.getCurrentAmount() + amount);
            DataStore.saveGoal(goal);
            refresh();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
        }
    }

    private void row(JPanel p, GridBagConstraints gc, int r, String lbl, JComponent field) {
        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 1; gc.weightx = 0.4;
        p.add(new JLabel(lbl), gc);
        gc.gridx = 1; gc.weightx = 0.6;
        p.add(field, gc);
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}