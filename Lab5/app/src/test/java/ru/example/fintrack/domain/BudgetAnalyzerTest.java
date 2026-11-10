package ru.example.fintrack.domain;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import ru.example.fintrack.data.entity.BudgetRule;
import static org.junit.Assert.assertEquals;

public class BudgetAnalyzerTest {
    @Test
    public void eightyFivePercentMeansNearLimit() {
        List<BudgetRule> rules = Arrays.asList(
                new BudgetRule(120, 3, "Существенное превышение", "#991B1B", "🚨"),
                new BudgetRule(100, 2, "Лимит превышен", "#DC2626", "⛔"),
                new BudgetRule(80, 1, "Приближение к лимиту", "#D97706", "⚠️"),
                new BudgetRule(0, 0, "В пределах нормы", "#16A34A", "✅")
        );

        BudgetStatus result = BudgetAnalyzer.analyze(1, "Продукты",
                85000, 100000, rules);

        assertEquals(85, result.percent);
        assertEquals(1, result.severity);
        assertEquals("Приближение к лимиту", result.title);
    }
}
