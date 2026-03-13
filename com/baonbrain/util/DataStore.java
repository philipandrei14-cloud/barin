package com.baonbrain.util;

import com.baonbrain.model.*;

import java.io.*;
import java.util.*;

public class DataStore {

    //File Paths
    private static final String DATA_DIR      = "data/";
    private static final String USERS_FILE    = DATA_DIR + "users.dat";
    private static final String INCOMES_FILE  = DATA_DIR + "incomes.dat";
    private static final String EXPENSES_FILE = DATA_DIR + "expenses.dat";
    private static final String BUDGETS_FILE  = DATA_DIR + "budgets.dat";
    private static final String GOALS_FILE    = DATA_DIR + "goals.dat";

    static {
        new File(DATA_DIR).mkdirs();
    }


    //GENERIC HELPERS
    @SuppressWarnings("unchecked")
    private static <T> List<T> loadList(String filename) {
        File f = new File(filename);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static <T> void saveList(String filename, List<T> list) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //USERS
    public static List<User> loadUsers() {
        return loadList(USERS_FILE);
    }

    public static void saveUsers(List<User> users) {
        saveList(USERS_FILE, users);
    }

    public static User findUser(String username, String password) {
        for (User u : loadUsers()) {
            if (u.getUsername().equals(username)
                    && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public static boolean usernameExists(String username) {
        for (User u : loadUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public static void saveUser(User user) {
        List<User> users = loadUsers();
        if (user.getId() == 0) {
            user.setId(users.stream()
                    .mapToInt(User::getId).max().orElse(0) + 1);
            users.add(user);
        } else {
            users.replaceAll(u ->
                    u.getId() == user.getId() ? user : u);
        }
        saveUsers(users);
    }


    //INCOMES
    public static List<Income> loadIncomes() {
        return loadList(INCOMES_FILE);
    }

    public static void saveAllIncomes(List<Income> list) {
        saveList(INCOMES_FILE, list);
    }

    public static List<Income> getIncomesByUser(int userId) {
        List<Income> result = new ArrayList<>();
        for (Income i : loadIncomes()) {
            if (i.getUserId() == userId) result.add(i);
        }
        return result;
    }

    public static void saveIncome(Income income) {
        List<Income> list = loadIncomes();
        if (income.getId() == 0) {
            income.setId(list.stream()
                    .mapToInt(Income::getId).max().orElse(0) + 1);
            list.add(income);
        } else {
            list.replaceAll(i ->
                    i.getId() == income.getId() ? income : i);
        }
        saveAllIncomes(list);
    }

    public static void deleteIncome(int id) {
        List<Income> list = loadIncomes();
        list.removeIf(i -> i.getId() == id);
        saveAllIncomes(list);
    }


    //EXPENSES
    public static List<Expense> loadExpenses() {
        return loadList(EXPENSES_FILE);
    }

    public static void saveAllExpenses(List<Expense> list) {
        saveList(EXPENSES_FILE, list);
    }

    public static List<Expense> getExpensesByUser(int userId) {
        List<Expense> result = new ArrayList<>();
        for (Expense e : loadExpenses()) {
            if (e.getUserId() == userId) result.add(e);
        }
        return result;
    }

    public static void saveExpense(Expense expense) {
        List<Expense> list = loadExpenses();
        if (expense.getId() == 0) {
            expense.setId(list.stream()
                    .mapToInt(Expense::getId).max().orElse(0) + 1);
            list.add(expense);
        } else {
            list.replaceAll(e ->
                    e.getId() == expense.getId() ? expense : e);
        }
        saveAllExpenses(list);
    }

    public static void deleteExpense(int id) {
        List<Expense> list = loadExpenses();
        list.removeIf(e -> e.getId() == id);
        saveAllExpenses(list);
    }


    //BUDGET LIMITS
    public static List<BudgetLimit> loadBudgets() {
        return loadList(BUDGETS_FILE);
    }

    public static void saveAllBudgets(List<BudgetLimit> list) {
        saveList(BUDGETS_FILE, list);
    }

    public static List<BudgetLimit> getBudgetsByUser(int userId) {
        List<BudgetLimit> result = new ArrayList<>();
        for (BudgetLimit b : loadBudgets()) {
            if (b.getUserId() == userId) result.add(b);
        }
        return result;
    }

    //Save the Budget
    public static void saveBudget(BudgetLimit budget) {
        List<BudgetLimit> list = loadBudgets();
        if (budget.getId() == 0) {
            budget.setId(list.stream()
                    .mapToInt(BudgetLimit::getId).max().orElse(0) + 1);
            list.add(budget);
        } else {
            list.replaceAll(b ->
                    b.getId() == budget.getId() ? budget : b);
        }
        saveAllBudgets(list);
    }

    //Deletes the Budget
    public static void deleteBudget(int id) {
        List<BudgetLimit> list = loadBudgets();
        list.removeIf(b -> b.getId() == id);
        saveAllBudgets(list);
    }


    //SAVINGS GOALS
    public static List<SavingsGoal> loadGoals() {
        return loadList(GOALS_FILE);
    }

    public static void saveAllGoals(List<SavingsGoal> list) {
        saveList(GOALS_FILE, list);
    }

    //Get goals
    public static List<SavingsGoal> getGoalsByUser(int userId) {
        List<SavingsGoal> result = new ArrayList<>();
        for (SavingsGoal g : loadGoals()) {
            if (g.getUserId() == userId) result.add(g);
        }
        return result;
    }

    //Save the goals
    public static void saveGoal(SavingsGoal goal) {
        List<SavingsGoal> list = loadGoals();
        if (goal.getId() == 0) {
            goal.setId(list.stream()
                    .mapToInt(SavingsGoal::getId).max().orElse(0) + 1);
            list.add(goal);
        } else {
            list.replaceAll(g ->
                    g.getId() == goal.getId() ? goal : g);
        }
        saveAllGoals(list);
    }

    public static void deleteGoal(int id) {
        List<SavingsGoal> list = loadGoals();
        list.removeIf(g -> g.getId() == id);
        saveAllGoals(list);
    }


    // 7. NOTES
    private static String notesFile(int userId) {
        return DATA_DIR + "notes" + userId + ".dat";
    }
    public static List<Note> loadNotes(int userId) {
        return loadList(notesFile(userId));
    }

    public static void saveAllNotes(int userId, List<Note> notes) {
        saveList(notesFile(userId), notes);
    }

    //Saves the note
    public static void saveNote(int userId, Note note) {
        List<Note> list = loadNotes(userId);
        if (note.id == 0) {
            note.id = list.stream()
                    .mapToInt(n -> n.id).max().orElse(0) + 1;
            list.add(note);
        } else {
            boolean found = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id == note.id) {
                    list.set(i, note);
                    found = true;
                    break;
                }
            }
            if (!found) list.add(note);
        }
        saveAllNotes(userId, list);
    }

    // DeleteNote
    public static void deleteNote(int userId, int noteId) {
        List<Note> list = loadNotes(userId);
        list.removeIf(n -> n.id == noteId);
        saveAllNotes(userId, list);
    }

    // NOTE DATA
    public static class Note implements Serializable {
        private static final long serialVersionUID = 1L;

        public int    id;
        public String title;
        public String content;
        public Date   date;

        public Note(int id, String title, String content, Date date) {
            this.id      = id;
            this.title   = title;
            this.content = content;
            this.date    = date;
        }
    }
}