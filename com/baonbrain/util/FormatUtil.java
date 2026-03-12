package com.baonbrain.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FormatUtil {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy");
    private static final SimpleDateFormat MONTH_KEY_FMT = new SimpleDateFormat("yyyy-MM");

    public static String currency(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }

    public static String date(Date date) {
        if (date == null) return "-";
        return DATE_FMT.format(date);
    }

    public static String monthKey(Date date) {
        return MONTH_KEY_FMT.format(date);
    }

    public static String monthKey(int year, int month) {
        return String.format("%04d-%02d", year, month);
    }

    public static int[] currentYearMonth() {
        Calendar c = Calendar.getInstance();
        return new int[]{c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1};
    }

    public static String[] getMonthNames() {
        return new String[]{"January","February","March","April","May","June",
                "July","August","September","October","November","December"};
    }
}