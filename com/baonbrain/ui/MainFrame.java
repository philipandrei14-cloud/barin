package com.baonbrain.ui;

import com.baonbrain.model.User;
import com.baonbrain.util.AppColors;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private final User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Panel instances
    private DashboardPanel dashboardPanel;
    private IncomePanel incomePanel;
    private ExpensePanel expensePanel;
    private BudgetPanel budgetPanel;
    private SavingsGoalPanel savingsPanel;
    private PredictionPanel predictionPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("BaonBrain - " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG_MAIN);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);

        setContentPane(root);
        showPanel("dashboard");
    }

    // ─── Sidebar ──────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(AppColors.PRIMARY);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(20, 50, 110));
        logoPanel.setBorder(new EmptyBorder(18, 16, 18, 16));
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        JLabel logo = new JLabel("💰 BaonBrain");
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logoPanel.add(logo);
        sidebar.add(logoPanel);

        // User info
        JPanel userInfo = new JPanel();
        userInfo.setBackground(AppColors.PRIMARY);
        userInfo.setBorder(new EmptyBorder(12, 16, 12, 16));
        userInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel("👤 " + currentUser.getFullName());
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(174, 214, 241));
        userInfo.add(nameLabel);
        sidebar.add(userInfo);

        sidebar.add(makeDivider());

        // Nav buttons
        sidebar.add(navButton("📊 Dashboard", "dashboard"));
        sidebar.add(navButton("💵 Income", "income"));
        sidebar.add(navButton("💸 Expenses", "expense"));
        sidebar.add(navButton("📋 Budget Limits", "budget"));
        sidebar.add(navButton("🎯 Savings Goals", "savings"));
        sidebar.add(navButton("🔮 Predictions", "prediction"));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(makeDivider());

        JButton logoutBtn = navButton("🚪 Logout", "logout");
        logoutBtn.setBackground(new Color(180, 40, 40));
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    private JButton navButton(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setBackground(AppColors.PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(AppColors.PRIMARY_LIGHT); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(AppColors.PRIMARY); }
        });

        btn.addActionListener(e -> {
            if ("logout".equals(panelName)) doLogout();
            else showPanel(panelName);
        });
        return btn;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(52, 110, 153));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ─── Content Panel ────────────────────────────────────────────────────

    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppColors.BG_MAIN);

        dashboardPanel  = new DashboardPanel(currentUser, this);
        incomePanel     = new IncomePanel(currentUser, this);
        expensePanel    = new ExpensePanel(currentUser, this);
        budgetPanel     = new BudgetPanel(currentUser, this);
        savingsPanel    = new SavingsGoalPanel(currentUser, this);
        predictionPanel = new PredictionPanel(currentUser, this);

        contentPanel.add(dashboardPanel,  "dashboard");
        contentPanel.add(incomePanel,     "income");
        contentPanel.add(expensePanel,    "expense");
        contentPanel.add(budgetPanel,     "budget");
        contentPanel.add(savingsPanel,    "savings");
        contentPanel.add(predictionPanel, "prediction");

        return contentPanel;
    }

    public void showPanel(String name) {
        cardLayout.show(contentPanel, name);
        // Refresh data when switching panels
        switch (name) {
            case "dashboard":  dashboardPanel.refresh();  break;
            case "income":     incomePanel.refresh();     break;
            case "expense":    expensePanel.refresh();    break;
            case "budget":     budgetPanel.refresh();     break;
            case "savings":    savingsPanel.refresh();    break;
            case "prediction": predictionPanel.refresh(); break;
        }
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}