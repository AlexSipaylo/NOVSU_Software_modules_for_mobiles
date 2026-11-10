package ru.example.fintrack.domain;

import java.util.List;
import ru.example.fintrack.data.entity.BudgetRule;

// Класс не зависит от Android и базы данных: он получает готовые данные и возвращает статус.
public final class BudgetAnalyzer {
    private BudgetAnalyzer() { }

    public static BudgetStatus analyze(long categoryId, String categoryName,
                                       long spent, long limit, List<BudgetRule> rules) {
        int percent = limit <= 0 ? 0 : (int) Math.min(1000, spent * 100 / limit);
        BudgetRule selected = null;

        // Выбираем правило с самым высоким достигнутым порогом.
        for (BudgetRule rule : rules) {
            if (percent >= rule.minPercent
                    && (selected == null || rule.minPercent > selected.minPercent)) {
                selected = rule;
            }
        }

        if (selected == null) {
            return new BudgetStatus(categoryId, categoryName, spent, limit,
                    limit - spent, percent, 0, "В пределах нормы", "#16A34A", "✅");
        }

        return new BudgetStatus(categoryId, categoryName, spent, limit,
                limit - spent, percent, selected.severity, selected.title, selected.color, selected.icon);
    }
}
