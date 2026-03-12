package com.baonbrain.service;

import com.baonbrain.model.Expense;
import com.baonbrain.util.FormatUtil;

import java.util.*;

/**
 * Predictive Expense Forecasting Service.
 * Uses simple moving-average logic on past monthly spending per category
 * to forecast the current month's spending.
 */
public class PredictionService {

    /**
     * Returns predicted spending per category for the current month
     * based on the average of past months (up to 3 months back).
     */
    public static Map<Expense.Category, Double> predictCurrentMonth(List<Expense> allExpenses) {
        Calendar cal = Calendar.getInstance();
        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH) + 1;

        // Build monthly totals per category for the past 3 months
        Map<String, Map<Expense.Category, Double>> monthlyTotals = new LinkedHashMap<>();

        for (int i = 1; i <= 3; i++) {
            int m = curMonth - i;
            int y = curYear;
            if (m <= 0) { m += 12; y--; }
            monthlyTotals.put(FormatUtil.monthKey(y, m), new HashMap<>());
        }

        for (Expense e : allExpenses) {
            cal.setTime(e.getDate());
            int ey = cal.get(Calendar.YEAR);
            int em = cal.get(Calendar.MONTH) + 1;
            String key = FormatUtil.monthKey(ey, em);
            if (monthlyTotals.containsKey(key)) {
                monthlyTotals.get(key).merge(e.getCategory(), e.getAmount(), Double::sum);
            }
        }

        // Average across available months
        Map<Expense.Category, Double> prediction = new LinkedHashMap<>();
        for (Expense.Category cat : Expense.Category.values()) {
            double total = 0;
            int count = 0;
            for (Map<Expense.Category, Double> monthly : monthlyTotals.values()) {
                if (monthly.containsKey(cat)) {
                    total += monthly.get(cat);
                    count++;
                }
            }
            if (count > 0) prediction.put(cat, total / count);
        }
        return prediction;
    }

    /**
     * Returns warnings: categories where current-month spending > 80% of budget limit.
     */
    public static List<String> generateWarnings(
            Map<Expense.Category, Double> spent,
            Map<Expense.Category, Double> limits) {

        List<String> warnings = new ArrayList<>();
        for (Map.Entry<Expense.Category, Double> entry : spent.entrySet()) {
            Expense.Category cat = entry.getKey();
            double spentAmt = entry.getValue();
            if (limits.containsKey(cat)) {
                double limit = limits.get(cat);
                double pct = limit > 0 ? (spentAmt / limit) * 100 : 0;
                if (pct >= 100) {
                    warnings.add("🚨 " + cat.name() + ": OVER budget! Spent " + formatPHP(spentAmt) + " / limit " + formatPHP(limit));
                } else if (pct >= 80) {
                    warnings.add("⚠️ " + cat.name() + ": " + String.format("%.0f%%", pct) + " of budget used (" + formatPHP(spentAmt) + " / " + formatPHP(limit) + ")");
                }
            }
        }
        return warnings;
    }

    private static String formatPHP(double v) {
        return String.format("₱%,.2f", v);
    }

    /**
     * Forecast remaining days spending for current month.
     */
    public static double forecastRemainingSpend(List<Expense> expenses) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int today = cal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysLeft = daysInMonth - today;

        double spentThisMonth = 0;
        for (Expense e : expenses) {
            cal.setTime(e.getDate());
            if (cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month) {
                spentThisMonth += e.getAmount();
            }
        }

        if (today == 0) return 0;
        double dailyRate = spentThisMonth / today;
        return dailyRate * daysLeft;
    }
}