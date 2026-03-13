package com.baonbrain.ui;

import com.baonbrain.model.*;
import com.baonbrain.service.FinancialService;
import com.baonbrain.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BudgetPanel extends JPanel {

    private final User user;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> monthCombo, yearCombo;

    private static final String[] COLS = {"Category", "Budget Limit", "Spent This Month", "Remaining", "Status"};

    //BudgetPanel Frame
    public BudgetPanel(User user, MainFrame mainFrame) {
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
        JLabel title = new JLabel("📋 Budget Limits");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleBar.add(title);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(AppColors.BG_MAIN);
        toolbar.setBorder(new EmptyBorder(0, 16, 0, 16));

        String[] months = FormatUtil.getMonthNames();
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));

        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = {String.valueOf(curYear - 1), String.valueOf(curYear), String.valueOf(curYear + 1)};
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(String.valueOf(curYear));

        JButton loadBtn = btn("Load", AppColors.PRIMARY);
        loadBtn.addActionListener(e -> refresh());
        JButton setBtn = btn("+ Set Budget", AppColors.WARNING);
        setBtn.addActionListener(e -> openSetDialog());
        JButton delBtn = btn("🗑 Delete", AppColors.DANGER);
        delBtn.addActionListener(e -> deleteSelected());

        toolbar.add(new JLabel("Month:"));
        toolbar.add(monthCombo);
        toolbar.add(new JLabel("Year:"));
        toolbar.add(yearCombo);
        toolbar.add(loadBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(setBtn);
        toolbar.add(delBtn);

        north.add(titleBar, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(AppColors.WARNING);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(AppColors.BORDER);
        table.setDefaultRenderer(Object.class, new StatusRenderer());

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
        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        Map<Expense.Category, Double> limits = getLimitsForMonth(year, month);
        Map<Expense.Category, Double> spent  = getSpentThisMonth(year, month);

        for (Expense.Category cat : Expense.Category.values()) {
            if (!limits.containsKey(cat)) continue;
            double limit = limits.get(cat);
            double spentAmt = spent.getOrDefault(cat, 0.0);
            double remaining = limit - spentAmt;
            String status;
            if (spentAmt >= limit) status = "OVER BUDGET";
            else if (spentAmt >= limit * 0.8) status = "WARNING";
            else status = "OK";

            tableModel.addRow(new Object[]{
                    cat.name(),
                    FormatUtil.currency(limit),
                    FormatUtil.currency(spentAmt),
                    FormatUtil.currency(remaining),
                    status
            });
        }
    }
    //limits
    private Map<Expense.Category, Double> getLimitsForMonth(int year, int month) {
        Map<Expense.Category, Double> map = new LinkedHashMap<>();
        for (BudgetLimit b : DataStore.getBudgetsByUser(user.getId())) {
            if (b.getYear() == year && b.getMonth() == month)
                map.put(b.getCategory(), b.getLimitAmount());
        }
        return map;
    }
    //Spent
    private Map<Expense.Category, Double> getSpentThisMonth(int year, int month) {
        Map<Expense.Category, Double> map = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        for (Expense e : DataStore.getExpensesByUser(user.getId())) {
            cal.setTime(e.getDate());
            if (cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month)
                map.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        return map;
    }
    //Set Budget
    private void openSetDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Budget Limit", true);
        d.setSize(360, 260);
        d.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 5, 6, 5);

        String[] catNames = Arrays.stream(Expense.Category.values()).map(Expense.Category::name).toArray(String[]::new);
        JComboBox<String> catCombo = new JComboBox<>(catNames);
        JTextField limitField = new JTextField();
        String[] months = FormatUtil.getMonthNames();
        JComboBox<String> mCombo = new JComboBox<>(months);
        mCombo.setSelectedIndex(monthCombo.getSelectedIndex());
        JComboBox<String> yCombo = new JComboBox<>(new String[]{
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1),
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1)
        });
        yCombo.setSelectedItem(yearCombo.getSelectedItem());

        addFormRow(panel, gc, 0, "Category:", catCombo);
        addFormRow(panel, gc, 1, "Limit Amount (₱):", limitField);
        addFormRow(panel, gc, 2, "Month:", mCombo);
        addFormRow(panel, gc, 3, "Year:", yCombo);

        JButton save = btn("Save Budget", AppColors.WARNING);
        save.addActionListener(e -> {
            try {
                double limit = Double.parseDouble(limitField.getText().trim());
                if (limit <= 0) throw new Exception("Limit must be positive.");
                Expense.Category cat = Expense.Category.values()[catCombo.getSelectedIndex()];
                int m = mCombo.getSelectedIndex() + 1;
                int y = Integer.parseInt((String) yCombo.getSelectedItem());
                BudgetLimit bl = new BudgetLimit(0, user.getId(), cat, limit, m, y);
                DataStore.saveBudget(bl);
                refresh();
                d.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Invalid amount.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage());
            }
        });
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        panel.add(save, gc);
        d.add(panel);
        d.setVisible(true);
    }

    //detes the budget
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row."); return; }
        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());
        String catName = (String) tableModel.getValueAt(row, 0);
        Expense.Category cat = Expense.Category.valueOf(catName);
        java.util.List<BudgetLimit> list = DataStore.getBudgetsByUser(user.getId());
        list.stream().filter(b -> b.getCategory() == cat && b.getMonth() == month && b.getYear() == year)
                .findFirst().ifPresent(b -> DataStore.deleteBudget(b.getId()));
        refresh();
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0.4;
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

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                 boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel && col == 4 && v != null) {
                switch (v.toString()) {
                    case "OVER BUDGET": c.setForeground(AppColors.DANGER); c.setBackground(new Color(255, 230, 230)); break;
                    case "WARNING":     c.setForeground(AppColors.WARNING); c.setBackground(new Color(255, 248, 225)); break;
                    default:            c.setForeground(AppColors.ACCENT);  c.setBackground(new Color(230, 255, 240)); break;
                }
            } else if (!sel) {
                c.setBackground(Color.WHITE);
                c.setForeground(AppColors.TEXT_DARK);
            }
            return c;
        }
    }
}