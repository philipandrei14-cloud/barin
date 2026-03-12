package com.baonbrain.ui;

import com.baonbrain.model.*;
import com.baonbrain.service.FinancialService;
import com.baonbrain.service.PredictionService;
import com.baonbrain.util.AppColors;
import com.baonbrain.util.DataStore;
import com.baonbrain.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final User user;
    private final MainFrame mainFrame;

    // Stat cards
    private JLabel totalIncomeVal, totalExpenseVal, balanceVal, savingsGoalsVal;
    private JPanel warningsPanel;
    private JPanel trendChartPanel;
    private JPanel categoryChartPanel;

    public DashboardPanel(User user, MainFrame mainFrame) {
        this.user = user;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        initUI();
    }

    private void initUI() {
        // Title bar
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBar.setBackground(AppColors.BG_MAIN);
        titleBar.setBorder(new EmptyBorder(16, 20, 4, 20));
        JLabel title = new JLabel("📊 Financial Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(AppColors.TEXT_DARK);
        titleBar.add(title);

        // Main scrollable content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new EmptyBorder(0, 16, 16, 16));

        // Stat cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsRow.setBackground(AppColors.BG_MAIN);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        totalIncomeVal  = new JLabel("₱0.00");
        totalExpenseVal = new JLabel("₱0.00");
        balanceVal      = new JLabel("₱0.00");
        savingsGoalsVal = new JLabel("0");

        cardsRow.add(statCard("💵 Total Income",  totalIncomeVal,  AppColors.ACCENT));
        cardsRow.add(statCard("💸 Total Expenses", totalExpenseVal, AppColors.DANGER));
        cardsRow.add(statCard("💰 Balance",        balanceVal,      AppColors.PRIMARY));
        cardsRow.add(statCard("🎯 Savings Goals",  savingsGoalsVal, AppColors.WARNING));

        content.add(cardsRow);
        content.add(Box.createVerticalStrut(14));

        // Warnings
        warningsPanel = new JPanel();
        warningsPanel.setLayout(new BoxLayout(warningsPanel, BoxLayout.Y_AXIS));
        warningsPanel.setBackground(AppColors.BG_MAIN);
        content.add(warningsPanel);
        content.add(Box.createVerticalStrut(8));

        // Charts row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsRow.setBackground(AppColors.BG_MAIN);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        trendChartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTrendChart(g);
            }
        };
        trendChartPanel.setBackground(Color.WHITE);
        trendChartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AppColors.BORDER), "Monthly Expense Trend (6 mo)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        categoryChartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart(g);
            }
        };
        categoryChartPanel.setBackground(Color.WHITE);
        categoryChartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AppColors.BORDER), "Expenses by Category (This Month)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        chartsRow.add(trendChartPanel);
        chartsRow.add(categoryChartPanel);
        content.add(chartsRow);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(titleBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLbl.setForeground(AppColors.TEXT_MUTED);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valueLabel.setForeground(accent);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setBackground(Color.WHITE);
        left.add(titleLbl, BorderLayout.NORTH);
        left.add(valueLabel, BorderLayout.CENTER);

        // Accent stripe on left edge
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(4, 0));

        card.add(stripe, BorderLayout.WEST);
        card.add(left, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        double income  = FinancialService.getTotalIncome(user.getId());
        double expense = FinancialService.getTotalExpenses(user.getId());
        double balance = FinancialService.getBalance(user.getId());
        int goals      = DataStore.getGoalsByUser(user.getId()).size();

        totalIncomeVal.setText(FormatUtil.currency(income));
        totalExpenseVal.setText(FormatUtil.currency(expense));
        balanceVal.setText(FormatUtil.currency(balance));
        balanceVal.setForeground(balance >= 0 ? AppColors.ACCENT : AppColors.DANGER);
        savingsGoalsVal.setText(String.valueOf(goals));

        // Warnings
        warningsPanel.removeAll();
        int[] ym = FormatUtil.currentYearMonth();
        Map<Expense.Category, Double> spent  = FinancialService.getCategoryTotalsThisMonth(user.getId());
        Map<Expense.Category, Double> limits = FinancialService.getBudgetLimitMap(user.getId(), ym[0], ym[1]);
        List<String> warnings = PredictionService.generateWarnings(spent, limits);
        for (String w : warnings) {
            JLabel lbl = new JLabel(w);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setForeground(w.startsWith("🚨") ? AppColors.DANGER : AppColors.WARNING);
            lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
            warningsPanel.add(lbl);
        }

        trendChartPanel.repaint();
        categoryChartPanel.repaint();
        revalidate();
    }

    // ─── Trend Bar Chart ──────────────────────────────────────────────────
    private void drawTrendChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, Double> trend = FinancialService.getMonthlyTrend(user.getId(), 6);
        if (trend.isEmpty()) return;

        int pw = trendChartPanel.getWidth();
        int ph = trendChartPanel.getHeight();
        int padL = 60, padR = 20, padT = 30, padB = 40;
        int chartW = pw - padL - padR;
        int chartH = ph - padT - padB;

        double maxVal = trend.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        g2.setColor(AppColors.BORDER);
        g2.drawRect(padL, padT, chartW, chartH);

        String[] labels = trend.keySet().toArray(new String[0]);
        Double[] values = trend.values().toArray(new Double[0]);
        int n = labels.length;
        int barW = Math.max(10, (chartW - (n + 1) * 8) / n);

        for (int i = 0; i < n; i++) {
            int x = padL + i * (chartW / n) + (chartW / n - barW) / 2;
            int barH = (int) ((values[i] / maxVal) * chartH);
            int y = padT + chartH - barH;

            g2.setColor(AppColors.PRIMARY_LIGHT);
            g2.fillRect(x, y, barW, barH);
            g2.setColor(AppColors.PRIMARY);
            g2.drawRect(x, y, barW, barH);

            g2.setColor(AppColors.TEXT_DARK);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(labels[i], x, padT + chartH + 14);

            if (values[i] > 0) {
                String val = String.format("%.0f", values[i]);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.drawString(val, x, y - 3);
            }
        }

        // Y-axis label
        g2.setColor(AppColors.TEXT_MUTED);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.drawString("₱" + (int) maxVal, 2, padT + 10);
        g2.drawString("₱0", 2, padT + chartH);
    }

    // ─── Pie Chart ────────────────────────────────────────────────────────
    private void drawPieChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<Expense.Category, Double> totals = FinancialService.getCategoryTotalsThisMonth(user.getId());
        double total = totals.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) {
            g2.setColor(AppColors.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("No expenses this month.", 30, categoryChartPanel.getHeight() / 2);
            return;
        }

        int pw = categoryChartPanel.getWidth();
        int ph = categoryChartPanel.getHeight();
        int size = Math.min(pw / 2, ph - 60);
        int cx = pw / 4, cy = ph / 2;
        int arc = 0;

        for (Map.Entry<Expense.Category, Double> entry : totals.entrySet()) {
            if (entry.getValue() == 0) continue;
            int sweep = (int) Math.round((entry.getValue() / total) * 360);
            g2.setColor(AppColors.getCategoryColor(entry.getKey()));
            g2.fillArc(cx - size / 2, cy - size / 2, size, size, arc, sweep);
            g2.setColor(Color.WHITE);
            g2.drawArc(cx - size / 2, cy - size / 2, size, size, arc, sweep);
            arc += sweep;
        }

        // Legend
        int lx = pw / 2 + 10, ly = 40;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (Map.Entry<Expense.Category, Double> entry : totals.entrySet()) {
            if (entry.getValue() == 0) continue;
            g2.setColor(AppColors.getCategoryColor(entry.getKey()));
            g2.fillRect(lx, ly, 12, 12);
            g2.setColor(AppColors.TEXT_DARK);
            double pct = (entry.getValue() / total) * 100;
            g2.drawString(entry.getKey().name() + " " + String.format("%.0f%%", pct), lx + 16, ly + 11);
            ly += 18;
            if (ly > ph - 20) break;
        }
    }
}