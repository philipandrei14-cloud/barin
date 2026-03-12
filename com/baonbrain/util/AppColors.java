package com.baonbrain.util;

import java.awt.*;

public class AppColors {
    public static final Color PRIMARY       = new Color(33, 97, 140);   // Deep Blue
    public static final Color PRIMARY_LIGHT = new Color(52, 152, 219);  // Sky Blue
    public static final Color ACCENT        = new Color(39, 174, 96);   // Green
    public static final Color DANGER        = new Color(231, 76, 60);   // Red
    public static final Color WARNING       = new Color(243, 156, 18);  // Orange
    public static final Color BG_MAIN       = new Color(245, 248, 250); // Light Gray
    public static final Color BG_PANEL      = Color.WHITE;
    public static final Color TEXT_DARK     = new Color(30, 39, 46);
    public static final Color TEXT_MUTED    = new Color(113, 128, 147);
    public static final Color BORDER        = new Color(210, 218, 226);

    // Category colors
    public static Color getCategoryColor(com.baonbrain.model.Expense.Category cat) {
        switch (cat) {
            case FOOD:       return new Color(231, 76, 60);
            case TRANSPORT:  return new Color(52, 152, 219);
            case LEISURE:    return new Color(155, 89, 182);
            case SCHOOL:     return new Color(39, 174, 96);
            case HEALTH:     return new Color(26, 188, 156);
            case CLOTHING:   return new Color(243, 156, 18);
            case UTILITIES:  return new Color(149, 165, 166);
            default:         return new Color(189, 195, 199);
        }
    }
}