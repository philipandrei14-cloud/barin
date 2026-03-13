package com.baonbrain.model;

import java.io.Serializable;

public class BudgetLimit implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private Expense.Category category;
    private double limitAmount;
    private int month; // 1-12
    private int year;

    public BudgetLimit(int i, int id, Expense.Category cat, double limit, int m, int y) {}

    public BudgetLimit(int id, int userId, Expense.Category category, double limitAmount, int month, int year,double time) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.limitAmount = limitAmount;
        this.month = month;
        this.year = year;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Expense.Category getCategory() { return category; }
    public void setCategory(Expense.Category category) { this.category = category; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}