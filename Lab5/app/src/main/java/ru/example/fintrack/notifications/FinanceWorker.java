package ru.example.fintrack.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.List;
import ru.example.fintrack.data.dao.FinanceDao;
import ru.example.fintrack.data.db.FinanceDatabase;
import ru.example.fintrack.data.entity.BudgetRule;
import ru.example.fintrack.data.entity.Category;
import ru.example.fintrack.data.entity.CategoryLimit;
import ru.example.fintrack.data.entity.RecurringPayment;
import ru.example.fintrack.domain.BudgetAnalyzer;
import ru.example.fintrack.domain.BudgetStatus;
import ru.example.fintrack.util.DateUtils;
import ru.example.fintrack.util.MoneyUtils;

// Один Worker выполняет два простых вида задач: контроль лимита и напоминание о платеже.
public class FinanceWorker extends Worker {
    public FinanceWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override public Result doWork() {
        FinanceDao dao = FinanceDatabase.getInstance(getApplicationContext()).financeDao();
        String kind = getInputData().getString("kind");
        if ("LIMIT".equals(kind)) checkLimit(dao);
        if ("PAYMENT".equals(kind)) remindPayment(dao);
        return Result.success();
    }

    private void checkLimit(FinanceDao dao) {
        long categoryId = getInputData().getLong("categoryId", -1);
        String month = getInputData().getString("month");
        String currentMonth = DateUtils.monthKey(System.currentTimeMillis());
        if (categoryId <= 0 || month == null || !month.equals(currentMonth)) return;

        Category category = dao.getCategory(categoryId);
        CategoryLimit limit = dao.getLimit(categoryId, month);
        if (category == null || limit == null || limit.limitKopecks <= 0) return;

        long now = System.currentTimeMillis();
        long spent = dao.getExpenses(categoryId, DateUtils.monthStart(now), DateUtils.nextMonthStart(now));
        List<BudgetRule> rules = dao.getRules();
        BudgetStatus status = BudgetAnalyzer.analyze(categoryId, category.name,
                spent, limit.limitKopecks, rules);

        SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences("budget_status", Context.MODE_PRIVATE);
        String key = month + "_" + categoryId;
        int previousSeverity = preferences.getInt(key, 0);

        if (status.severity > previousSeverity && status.severity > 0) {
            String message = category.name + ": потрачено " + MoneyUtils.format(spent)
                    + " из " + MoneyUtils.format(limit.limitKopecks) + ". " + status.title + ".";
            NotificationHelper.show(getApplicationContext(), (int) (1000 + categoryId),
                    "Контроль бюджета", message, "BUDGET");
        }
        preferences.edit().putInt(key, status.severity).apply();
    }

    private void remindPayment(FinanceDao dao) {
        long id = getInputData().getLong("paymentId", -1);
        RecurringPayment payment = dao.getPayment(id);
        if (payment == null || !payment.active) return;

        NotificationHelper.show(getApplicationContext(), (int) (2000 + id),
                "Напоминание о платеже",
                payment.title + " — " + MoneyUtils.format(payment.amountKopecks)
                        + ". Срок: " + DateUtils.format(payment.dueDate) + ".", "PAYMENTS");

        // После уведомления назначаем следующий месячный платёж.
        do {
            payment.dueDate = DateUtils.nextPaymentDate(payment.dueDate);
        } while (payment.dueDate <= System.currentTimeMillis());
        dao.updatePayment(payment);
        ReminderScheduler.schedulePayment(getApplicationContext(), payment);
    }
}
