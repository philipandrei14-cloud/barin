package com.baonbrain.ui;

import com.baonbrain.model.*;
import com.baonbrain.service.*;
import com.baonbrain.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public class PredictionPanel extends JPanel {

    private final User user;
    private final MainFrame mainFrame;
    private JPanel contentPanel;

    public PredictionPanel(User user, MainFrame mainFrame) {
        this.user = user;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.setBackground(AppColors.BG_MAIN);
        north.setBorder(new EmptyBorder(16, 20, 8, 20));
        JLabel title = new JLabel("🔮 Predictive Expense Forecasting");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        north.add(title);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AppColors.BG_MAIN);
        contentPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(north, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        contentPanel.removeAll();

        java.util.List<Expense> allExpenses = DataStore.getExpensesByUser(user.getId());
        int[] ym = FormatUtil.currentYearMonth();
        Map<Expense.Category, Double> spent  = FinancialService.getCategoryTotalsThisMonth(user.getId());
        Map<Expense.Category, Double> limits  = FinancialService.getBudgetLimitMap(user.getId(), ym[0], ym[1]);
        Map<Expense.Category, Double> predicted = PredictionService.predictCurrentMonth(allExpenses);
        double forecastRemaining = PredictionService.forecastRemainingSpend(allExpenses);
        java.util.List<String> warnings = PredictionService.generateWarnings(spent, limits);

        // ── Summary Cards Row ──
        JPanel row1 = new JPanel(new GridLayout(1, 3, 12, 0));
        row1.setBackground(AppColors.BG_MAIN);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        double totalSpentThisMonth = spent.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalIncome = FinancialService.getMonthlyIncome(user.getId(), ym[0], ym[1]);

        row1.add(summaryCard("💸 Spent This Month", FormatUtil.currency(totalSpentThisMonth), AppColors.DANGER));
        row1.add(summaryCard("🔮 Forecast (Rest of Month)", FormatUtil.currency(forecastRemaining), AppColors.WARNING));
        row1.add(summaryCard("💵 Monthly Income", FormatUtil.currency(totalIncome), AppColors.ACCENT));
        contentPanel.add(row1);
        contentPanel.add(Box.createVerticalStrut(14));

        // ── Warnings ──
        if (!warnings.isEmpty()) {
            JPanel warnPanel = new JPanel();
            warnPanel.setLayout(new BoxLayout(warnPanel, BoxLayout.Y_AXIS));
            warnPanel.setBackground(new Color(255, 245, 220));
            warnPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.WARNING),
                    new EmptyBorder(10, 14, 10, 14)));
            warnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            JLabel wTitle = new JLabel("⚠️ Budget Warnings");
            wTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
            wTitle.setForeground(AppColors.WARNING);
            warnPanel.add(wTitle);
            warnPanel.add(Box.createVerticalStrut(6));
            for (String w : warnings) {
                JLabel lbl = new JLabel(w);
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
                lbl.setForeground(w.startsWith("🚨") ? AppColors.DANGER : AppColors.WARNING);
                warnPanel.add(lbl);
            }
            contentPanel.add(warnPanel);
            contentPanel.add(Box.createVerticalStrut(14));
        }

        // ── Category Prediction Table ──
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                "Category Spending vs Prediction (Based on Last 3 Months)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13)));

        String[] cols = {"Category", "Spent This Month", "Predicted (Avg)", "Budget Limit", "Insight"};
        Object[][] data = buildTableData(spent, predicted, limits);
        javax.swing.JTable tbl = new javax.swing.JTable(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl.setRowHeight(28);
        tbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tbl.getTableHeader().setBackground(new Color(93, 63, 211));
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.setGridColor(AppColors.BORDER);
        tbl.setDefaultRenderer(Object.class, new InsightRenderer());

        tablePanel.add(tbl.getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(tbl, BorderLayout.CENTER);
        contentPanel.add(tablePanel);
        contentPanel.add(Box.createVerticalStrut(14));

        // ── Tips ──
        JPanel tipsPanel = buildTipsPanel(totalSpentThisMonth, totalIncome, warnings.size());
        contentPanel.add(tipsPanel);
        contentPanel.add(Box.createVerticalGlue());

        revalidate();
        repaint();
    }

    private Object[][] buildTableData(Map<Expense.Category, Double> spent,
                                      Map<Expense.Category, Double> predicted,
                                      Map<Expense.Category, Double> limits) {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (Expense.Category cat : Expense.Category.values()) {
            double s = spent.getOrDefault(cat, 0.0);
            double p = predicted.getOrDefault(cat, 0.0);
            double l = limits.getOrDefault(cat, 0.0);
            if (s == 0 && p == 0) continue;

            String insight;
            if (l > 0 && s > l) insight = "🚨 Over Budget";
            else if (l > 0 && s > l * 0.8) insight = "⚠️ Near Limit";
            else if (p > 0 && s > p * 1.2) insight = "📈 Spending More Than Usual";
            else if (p > 0 && s < p * 0.5) insight = "✅ Under Average";
            else insight = "✔ Normal";

            rows.add(new Object[]{
                    cat.name(),
                    FormatUtil.currency(s),
                    p > 0 ? FormatUtil.currency(p) : "No history",
                    l > 0 ? FormatUtil.currency(l) : "Not set",
                    insight
            });
        }
        return rows.toArray(new Object[0][]);
    }

    private JPanel buildTipsPanel(double spent, double income, int warningCount) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(235, 245, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.PRIMARY_LIGHT),
                new EmptyBorder(12, 16, 12, 16)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel t = new JLabel("💡 Personalized Financial Insights");
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        t.setForeground(AppColors.PRIMARY);
        panel.add(t);
        panel.add(Box.createVerticalStrut(8));

        java.util.List<String> tips = new java.util.ArrayList<>();
        if (income > 0 && spent > income * 0.9)
            tips.add("• You're spending over 90% of your monthly income. Try to save at least 10-20%.");
        if (warningCount > 0)
            tips.add("• You have " + warningCount + " category(ies) near or over budget. Review your spending.");
        if (income == 0)
            tips.add("• No income recorded this month. Make sure to log your allowance and earnings.");
        tips.add("• Tip: Record expenses daily to get more accurate predictions.");
        tips.add("• Tip: Set budget limits for every category to receive early warnings.");

        for (String tip : tips) {
            JLabel lbl = new JLabel(tip);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lbl.setForeground(AppColors.TEXT_DARK);
            panel.add(lbl);
            panel.add(Box.createVerticalStrut(4));
        }
        return panel;
    }

    private JPanel summaryCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppColors.BORDER),
                new EmptyBorder(12, 14, 12, 14)));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(AppColors.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 18));
        v.setForeground(accent);
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(4, 0));
        card.add(stripe, BorderLayout.WEST);
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private static class InsightRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(javax.swing.JTable t, Object v,
                                                                 boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel && col == 4 && v != null) {
                String s = v.toString();
                if (s.contains("Over"))  { c.setForeground(AppColors.DANGER);  c.setBackground(new Color(255,230,230)); }
                else if (s.contains("Near")) { c.setForeground(AppColors.WARNING); c.setBackground(new Color(255,248,220)); }
                else if (s.contains("More")) { c.setForeground(new Color(155, 89, 182)); c.setBackground(new Color(248,235,255)); }
                else { c.setForeground(AppColors.ACCENT); c.setBackground(new Color(230,255,240)); }
            } else if (!sel) {
                c.setBackground(Color.WHITE);
                c.setForeground(AppColors.TEXT_DARK);
            }
            return c;
        }
    }
}