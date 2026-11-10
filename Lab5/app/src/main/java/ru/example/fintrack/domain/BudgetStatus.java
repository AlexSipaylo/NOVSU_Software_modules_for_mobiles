package ru.example.fintrack.domain;

public class BudgetStatus {
    public final long categoryId;
    public final String categoryName;
    public final long spent;
    public final long limit;
    public final long remaining;
    public final int percent;
    public final int severity;
    public final String title;
    public final String color;
    public final String icon;

    public BudgetStatus(long categoryId, String categoryName, long spent, long limit,
                        long remaining, int percent, int severity, String title, String color, String icon) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.spent = spent;
        this.limit = limit;
        this.remaining = remaining;
        this.percent = percent;
        this.severity = severity;
        this.title = title;
        this.color = color;
        this.icon = icon;
    }
}
