package ru.example.fintrack.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ru.example.fintrack.data.dao.FinanceDao;
import ru.example.fintrack.data.db.FinanceDatabase;
import ru.example.fintrack.data.entity.Account;
import ru.example.fintrack.data.entity.BudgetRule;
import ru.example.fintrack.data.entity.Category;
import ru.example.fintrack.data.entity.CategoryLimit;
import ru.example.fintrack.data.entity.FinanceTransaction;
import ru.example.fintrack.data.entity.RecurringPayment;
import ru.example.fintrack.notifications.ReminderScheduler;
import ru.example.fintrack.util.DateUtils;

// Единственное место, где экранные классы запускают операции записи в Room.
public class FinanceRepository {
    public interface Callback<T> {
        void onComplete(T value, Exception error);
    }

    private final FinanceDao dao;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public FinanceRepository(Context context) {
        this.context = context.getApplicationContext();
        dao = FinanceDatabase.getInstance(this.context).financeDao();
    }

    public LiveData<List<Category>> categories() { return dao.observeCategories(); }
    public LiveData<List<FinanceTransaction>> transactions() { return dao.observeTransactions(); }
    public LiveData<List<BudgetRule>> rules() { return dao.observeRules(); }
    public LiveData<List<CategoryLimit>> limits(String month) { return dao.observeLimits(month); }
    public LiveData<List<RecurringPayment>> payments() { return dao.observePayments(); }

    public void saveTransaction(FinanceTransaction item, boolean isNew, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                FinanceTransaction old = isNew ? null : dao.getTransaction(item.id);
                if (isNew) {
                    Account account = dao.getMainAccount();
                    if (account == null) throw new IllegalStateException("Основной счёт не найден");
                    item.accountId = account.id;
                    item.id = dao.insertTransaction(item);
                } else {
                    dao.updateTransaction(item);
                }
                if (old != null) ReminderScheduler.checkLimit(context, old.categoryId,
                        DateUtils.monthKey(old.date));
                ReminderScheduler.checkLimit(context, item.categoryId, DateUtils.monthKey(item.date));
                finish(callback, null, null);
            } catch (Exception e) { finish(callback, null, e); }
        });
    }

    public void deleteTransaction(long id, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                FinanceTransaction item = dao.getTransaction(id);
                if (item != null) {
                    dao.deleteTransaction(item);
                    ReminderScheduler.checkLimit(context, item.categoryId, DateUtils.monthKey(item.date));
                }
                finish(callback, null, null);
            } catch (Exception e) { finish(callback, null, e); }
        });
    }

    public void saveLimit(CategoryLimit limit, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                dao.saveLimit(limit);
                ReminderScheduler.checkLimit(context, limit.categoryId, limit.month);
                finish(callback, null, null);
            } catch (Exception e) { finish(callback, null, e); }
        });
    }

    public void deleteLimit(long categoryId, String month, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                dao.deleteLimit(categoryId, month);
                context.getSharedPreferences("budget_status", Context.MODE_PRIVATE)
                        .edit().remove(month + "_" + categoryId).apply();
                finish(callback, null, null);
            } catch (Exception e) { finish(callback, null, e); }
        });
    }

    public void addPayment(RecurringPayment payment, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                payment.id = dao.insertPayment(payment);
                ReminderScheduler.schedulePayment(context, payment);
                finish(callback, null, null);
            } catch (Exception e) { finish(callback, null, e); }
        });
    }

    public void deletePayment(long id, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                RecurringPayment payment = dao.getPayment(id);
                if (payment != null) dao.deletePayment(payment);
                ReminderScheduler.cancelPayment(context, id);
                finish(callback, null, null);
            } catch (Exception e) { finish(callback, null, e); }
        });
    }

    private <T> void finish(Callback<T> callback, T value, Exception error) {
        mainHandler.post(() -> callback.onComplete(value, error));
    }
}
