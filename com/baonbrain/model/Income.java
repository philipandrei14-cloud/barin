package com.baonbrain.model;

import java.io.Serializable;
import java.util.Date;

public class Income implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum IncomeType {
        ALLOWANCE, PART_TIME_JOB, SIDE_HUSTLE, GIFT, OTHER
    }

    private int id;
    private int userId;
    private String description;
    private double amount;
    private IncomeType type;
    private Date date;

    public Income() {}

    public Income(int id, int userId, String description, double amount, IncomeType type, Date date) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
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

    public IncomeType getType() { return type; }
    public void setType(IncomeType type) { this.type = type; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getTypeDisplayName() {
        switch (type) {
            case ALLOWANCE: return "Allowance";
            case PART_TIME_JOB: return "Part-Time Job";
            case SIDE_HUSTLE: return "Side Hustle";
            case GIFT: return "Gift";
            default: return "Other";
        }
    }
}