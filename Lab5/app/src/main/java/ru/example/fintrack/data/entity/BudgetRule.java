package ru.example.fintrack.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Правила хранятся в таблице, чтобы пороги можно было менять без изменения алгоритма.
@Entity(tableName = "budget_rules")
public class BudgetRule {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int minPercent;
    public int severity;
    @NonNull public String title;
    @NonNull public String color;
    @NonNull public String icon;

    public BudgetRule() { this.title = ""; this.color = ""; this.icon = ""; }

    public BudgetRule(int minPercent, int severity, @NonNull String title,
                      @NonNull String color, @NonNull String icon) {
        this.minPercent = minPercent;
        this.severity = severity;
        this.title = title;
        this.color = color;
        this.icon = icon;
    }
}
