package com.baonbrain.model;

import java.io.Serializable;
import java.util.Date;

public class Expense implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Category {
        FOOD, TRANSPORT, LEISURE, SCHOOL, HEALTH, CLOTHING, UTILITIES, OTHER
    }

    private int id;
    private int userId;
    private String description;
    private double amount;
    private Category category;
    private Date date;

    public Expense() {}

    public Expense(int id, int userId, String description, double amount, Category category, Date date) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getCategoryDisplayName() {
        switch (category) {
            case FOOD: return "Food";
            case TRANSPORT: return "Transport";
            case LEISURE: return "Leisure";
            case SCHOOL: return "School";
            case HEALTH: return "Health";
            case CLOTHING: return "Clothing";
            case UTILITIES: return "Utilities";
            default: return "Other";
        }
    }
}