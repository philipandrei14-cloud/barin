package com.baonbrain.ui;

import com.baonbrain.model.*;
import com.baonbrain.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class IncomePanel extends JPanel {

    private final User user;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    private static final String[] COLS = {"#", "Description", "Type", "Amount", "Date"};

    public IncomePanel(User user, MainFrame mainFrame) {
        this.user = user;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        initUI();
    }

    private void initUI() {
        // Title
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(AppColors.BG_MAIN);
        titleBar.setBorder(new EmptyBorder(16, 20, 8, 20));
        JLabel title = new JLabel("💵 Income Records");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleBar.add(title, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(AppColors.BG_MAIN);
        toolbar.setBorder(new EmptyBorder(0, 16, 8, 16));

        JButton addBtn = actionButton("+ Add Income", AppColors.ACCENT);
        addBtn.addActionListener(e -> openAddDialog());
        JButton delBtn = actionButton("🗑 Delete", AppColors.DANGER);
        delBtn.addActionListener(e -> deleteSelected());

        totalLabel = new JLabel("Total: ₱0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalLabel.setForeground(AppColors.ACCENT);

        toolbar.add(addBtn);
        toolbar.add(delBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(totalLabel);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(AppColors.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(174, 214, 241));
        table.setGridColor(AppColors.BORDER);
        table.setShowGrid(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(AppColors.BG_MAIN);
        main.setBorder(new EmptyBorder(0, 16, 16, 16));
        main.add(scroll, BorderLayout.CENTER);

        add(titleBar, BorderLayout.NORTH);
        add(toolbar, BorderLayout.BEFORE_FIRST_LINE); // trick: use panel wrapper
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(AppColors.BG_MAIN);
        north.add(titleBar, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<Income> incomes = DataStore.getIncomesByUser(user.getId());
        incomes.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        double total = 0;
        int i = 1;
        for (Income inc : incomes) {
            tableModel.addRow(new Object[]{
                    i++,
                    inc.getDescription(),
                    inc.getTypeDisplayName(),
                    FormatUtil.currency(inc.getAmount()),
                    FormatUtil.date(inc.getDate())
            });
            total += inc.getAmount();
        }
        totalLabel.setText("Total Income: " + FormatUtil.currency(total));
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Income", true);
        dialog.setSize(380, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);

        JTextField descField = new JTextField();
        JTextField amountField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Allowance", "Part-Time Job", "Side Hustle", "Gift", "Other"
        });
        JTextField dateField = new JTextField(FormatUtil.date(new Date()));

        int r = 0;
        addRow(panel, gc, r++, "Description:", descField);
        addRow(panel, gc, r++, "Amount (₱):", amountField);
        addRow(panel, gc, r++, "Type:", typeCombo);
        addRow(panel, gc, r++, "Date (MMM dd, yyyy):", dateField);

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(AppColors.ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setOpaque(true);

        saveBtn.addActionListener(e -> {
            try {
                String desc = descField.getText().trim();
                if (desc.isEmpty()) throw new Exception("Description is required.");
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new Exception("Amount must be positive.");
                Income.IncomeType[] types = Income.IncomeType.values();
                Income.IncomeType type = types[typeCombo.getSelectedIndex()];
                Income income = new Income(0, user.getId(), desc, amount, type, new Date());
                DataStore.saveIncome(income);
                refresh();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 2;
        panel.add(saveBtn, gc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this income record?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            List<Income> list = DataStore.getIncomesByUser(user.getId());
            list.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            DataStore.deleteIncome(list.get(row).getId());
            refresh();
        }
    }

    private int getSelectedRow() {
        Component center = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);

        Component[] comps = ((Container) center).getComponents();
        // Find JScrollPane -> JTable
        for (Component c : ((JPanel) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER)).getComponents()) {
            if (c instanceof JScrollPane) {
                return ((JTable) ((JScrollPane) c).getViewport().getView()).getSelectedRow();
            }
        }
        return -1;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0.3;
        p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 0.7;
        p.add(field, gc);
    }

    private JButton actionButton(String text, Color bg) {
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