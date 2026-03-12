package com.baonbrain.ui;

import com.baonbrain.model.*;
import com.baonbrain.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ExpensePanel extends JPanel {

    private final User user;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JTable table;

    private static final String[] COLS = {"#", "Description", "Category", "Amount", "Date"};

    public ExpensePanel(User user, MainFrame mainFrame) {
        this.user = user;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        initUI();
    }

    private void initUI() {
        // North
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(AppColors.BG_MAIN);

        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBar.setBackground(AppColors.BG_MAIN);
        titleBar.setBorder(new EmptyBorder(16, 20, 0, 20));
        JLabel title = new JLabel("💸 Expense Records");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleBar.add(title);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(AppColors.BG_MAIN);
        toolbar.setBorder(new EmptyBorder(0, 16, 0, 16));

        JButton addBtn = btn("+ Add Expense", AppColors.DANGER);
        addBtn.addActionListener(e -> openAddDialog());
        JButton delBtn = btn("🗑 Delete", new Color(120, 40, 40));
        delBtn.addActionListener(e -> deleteSelected());

        totalLabel = new JLabel("Total: ₱0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalLabel.setForeground(AppColors.DANGER);

        toolbar.add(addBtn);
        toolbar.add(delBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(totalLabel);

        north.add(titleBar, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(AppColors.DANGER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(250, 200, 200));
        table.setGridColor(AppColors.BORDER);
        table.setDefaultRenderer(Object.class, new CategoryColorRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(AppColors.BG_MAIN);
        center.setBorder(new EmptyBorder(4, 16, 16, 16));
        center.add(scroll, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<Expense> expenses = DataStore.getExpensesByUser(user.getId());
        expenses.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        double total = 0;
        int i = 1;
        for (Expense exp : expenses) {
            tableModel.addRow(new Object[]{
                    i++,
                    exp.getDescription(),
                    exp.getCategoryDisplayName(),
                    FormatUtil.currency(exp.getAmount()),
                    FormatUtil.date(exp.getDate())
            });
            total += exp.getAmount();
        }
        totalLabel.setText("Total Expenses: " + FormatUtil.currency(total));
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Expense", true);
        dialog.setSize(380, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);

        JTextField descField = new JTextField();
        JTextField amountField = new JTextField();

        String[] catNames = Arrays.stream(Expense.Category.values())
                .map(Expense.Category::name).toArray(String[]::new);
        JComboBox<String> catCombo = new JComboBox<>(catNames);

        int r = 0;
        addRow(panel, gc, r++, "Description:", descField);
        addRow(panel, gc, r++, "Amount (₱):", amountField);
        addRow(panel, gc, r++, "Category:", catCombo);

        JButton saveBtn = btn("Save Expense", AppColors.DANGER);
        saveBtn.addActionListener(e -> {
            try {
                String desc = descField.getText().trim();
                if (desc.isEmpty()) throw new Exception("Description required.");
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new Exception("Amount must be positive.");
                Expense.Category cat = Expense.Category.values()[catCombo.getSelectedIndex()];
                Expense exp = new Expense(0, user.getId(), desc, amount, cat, new Date());
                DataStore.saveExpense(exp);
                refresh();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid amount.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 2;
        panel.add(saveBtn, gc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Delete this expense?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            List<Expense> list = DataStore.getExpensesByUser(user.getId());
            list.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            DataStore.deleteExpense(list.get(row).getId());
            refresh();
        }
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0.35;
        p.add(new JLabel(lbl), gc);
        gc.gridx = 1; gc.weightx = 0.65;
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

    // Color rows by category
    private class CategoryColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel) {
                List<Expense> list = DataStore.getExpensesByUser(user.getId());
                list.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                if (row < list.size()) {
                    Color cat = AppColors.getCategoryColor(list.get(row).getCategory());
                    c.setBackground(new Color(cat.getRed(), cat.getGreen(), cat.getBlue(), 30));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}