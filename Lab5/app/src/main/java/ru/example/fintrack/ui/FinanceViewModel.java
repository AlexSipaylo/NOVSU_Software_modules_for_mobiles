package ru.example.fintrack.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.example.fintrack.data.FinanceRepository;
import ru.example.fintrack.data.entity.BudgetRule;
import ru.example.fintrack.data.entity.Category;
import ru.example.fintrack.data.entity.CategoryLimit;
import ru.example.fintrack.data.entity.FinanceTransaction;
import ru.example.fintrack.data.entity.RecurringPayment;
import ru.example.fintrack.domain.BudgetAnalyzer;
import ru.example.fintrack.domain.BudgetStatus;
import ru.example.fintrack.util.DateUtils;

public class FinanceViewModel extends AndroidViewModel {
    private final FinanceRepository repository;
    private final String month = DateUtils.monthKey(System.currentTimeMillis());
    private final long monthStart = DateUtils.monthStart(System.currentTimeMillis());
    private final long nextMonth = DateUtils.nextMonthStart(System.currentTimeMillis());
    private final MediatorLiveData<List<BudgetStatus>> statuses = new MediatorLiveData<>();

    private List<Category> categories = new ArrayList<>();
    private List<FinanceTransaction> transactions = new ArrayList<>();
    private List<CategoryLimit> limits = new ArrayList<>();
    private List<BudgetRule> rules = new ArrayList<>();

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
        statuses.addSource(repository.categories(), value -> { categories = value; calculate(); });
        statuses.addSource(repository.transactions(), value -> { transactions = value; calculate(); });
        statuses.addSource(repository.limits(month), value -> { limits = value; calculate(); });
        statuses.addSource(repository.rules(), value -> { rules = value; calculate(); });
    }

    // Считаем сумму расходов по каждой категории и передаём её анализатору лимитов.
    private void calculate() {
        Map<Long, Category> categoryById = new HashMap<>();
        for (Category category : categories) categoryById.put(category.id, category);

        Map<Long, Long> spentByCategory = new HashMap<>();
        for (FinanceTransaction item : transactions) {
            if ("EXPENSE".equals(item.type) && item.date >= monthStart && item.date < nextMonth) {
                long old = spentByCategory.getOrDefault(item.categoryId, 0L);
                spentByCategory.put(item.categoryId, old + item.amountKopecks);
            }
        }

        List<BudgetStatus> result = new ArrayList<>();
        for (CategoryLimit limit : limits) {
            Category category = categoryById.get(limit.categoryId);
            if (category != null) {
                result.add(BudgetAnalyzer.analyze(category.id, category.name,
                        spentByCategory.getOrDefault(category.id, 0L), limit.limitKopecks, rules));
            }
        }
        statuses.setValue(result);
    }

    public String month() { return month; }
    public LiveData<List<Category>> categories() { return repository.categories(); }
    public LiveData<List<FinanceTransaction>> transactions() { return repository.transactions(); }
    public LiveData<List<RecurringPayment>> payments() { return repository.payments(); }
    public LiveData<List<CategoryLimit>> limits() { return repository.limits(month); }
    public LiveData<List<BudgetStatus>> statuses() { return statuses; }

    public void saveTransaction(FinanceTransaction item, boolean isNew,
                                FinanceRepository.Callback<Void> callback) {
        repository.saveTransaction(item, isNew, callback);
    }
    public void deleteTransaction(long id, FinanceRepository.Callback<Void> callback) {
        repository.deleteTransaction(id, callback);
    }
    public void saveLimit(CategoryLimit item, FinanceRepository.Callback<Void> callback) {
        repository.saveLimit(item, callback);
    }
    public void deleteLimit(long categoryId, FinanceRepository.Callback<Void> callback) {
        repository.deleteLimit(categoryId, month, callback);
    }
    public void addPayment(RecurringPayment item, FinanceRepository.Callback<Void> callback) {
        repository.addPayment(item, callback);
    }
    public void deletePayment(long id, FinanceRepository.Callback<Void> callback) {
        repository.deletePayment(id, callback);
    }
}
