package com.baonbrain.service;

import com.baonbrain.model.*;
import com.baonbrain.util.DataStore;
import com.baonbrain.util.FormatUtil;

import java.util.*;

public class FinancialService {

    // ─── Summary helpers ──────────────────────────────────────────────────

    public static double getTotalIncome(int userId) {
        return DataStore.getIncomesByUser(userId).stream()
                .mapToDouble(Income::getAmount).sum();
    }

    public static double getTotalExpenses(int userId) {
        return DataStore.getExpensesByUser(userId).stream()
                .mapToDouble(Expense::getAmount).sum();
    }

    public static double getBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpenses(userId);
    }

    /** Total expenses this calendar month */
    public static double getMonthlyExpenses(int userId, int year, int month) {
        Calendar cal = Calendar.getInstance();
        double total = 0;
        for (Expense e : DataStore.getExpensesByUser(userId)) {
            cal.setTime(e.getDate());
            if (cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month) {
                total += e.getAmount();
            }
        }
        return total;
    }

    /** Total income this calendar month */
    public static double getMonthlyIncome(int userId, int year, int month) {
        Calendar cal = Calendar.getInstance();
        double total = 0;
        for (Income i : DataStore.getIncomesByUser(userId)) {
            cal.setTime(i.getDate());
            if (cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month) {
                total += i.getAmount();
            }
        }
        return total;
    }

    /** Expense totals per category this month */
    public static Map<Expense.Category, Double> getCategoryTotalsThisMonth(int userId) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        Map<Expense.Category, Double> map = new LinkedHashMap<>();
        for (Expense.Category c : Expense.Category.values()) map.put(c, 0.0);
        for (Expense e : DataStore.getExpensesByUser(userId)) {
            cal.setTime(e.getDate());
            if (cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month) {
                map.merge(e.getCategory(), e.getAmount(), Double::sum);
            }
        }
        return map;
    }

    /** Budget limits as a category→amount map for a given month */
    public static Map<Expense.Category, Double> getBudgetLimitMap(int userId, int year, int month) {
        Map<Expense.Category, Double> map = new HashMap<>();
        for (BudgetLimit b : DataStore.getBudgetsByUser(userId)) {
            if (b.getYear() == year && b.getMonth() == month) {
                map.put(b.getCategory(), b.getLimitAmount());
            }
        }
        return map;
    }

    /** Monthly expense totals over the last N months for the trend chart */
    public static Map<String, Double> getMonthlyTrend(int userId, int months) {
        Calendar cal = Calendar.getInstance();
        Map<String, Double> trend = new LinkedHashMap<>();
        String[] names = FormatUtil.getMonthNames();
        for (int i = months - 1; i >= 0; i--) {
            int m = cal.get(Calendar.MONTH) - i;
            int y = cal.get(Calendar.YEAR);
            while (m < 0) { m += 12; y--; }
            String label = names[m].substring(0, 3) + " " + (y % 100);
            trend.put(label, getMonthlyExpenses(userId, y, m + 1));
        }
        return trend;
    }
}