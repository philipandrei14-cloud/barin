package com.baonbrain.model;

import java.io.Serializable;
import java.util.Date;

public class SavingsGoal implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private Date targetDate;

    public SavingsGoal() {}

    public SavingsGoal(int id, int userId, String goalName, double targetAmount, double currentAmount, Date targetDate) {
        this.id = id;
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public Date getTargetDate() { return targetDate; }
    public void setTargetDate(Date targetDate) { this.targetDate = targetDate; }

    public double getProgressPercent() {
        if (targetAmount <= 0) return 0;
        return Math.min(100.0, (currentAmount / targetAmount) * 100.0);
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }
}