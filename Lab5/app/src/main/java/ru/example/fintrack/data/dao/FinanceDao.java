package ru.example.fintrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import java.util.List;
import ru.example.fintrack.data.entity.Account;
import ru.example.fintrack.data.entity.BudgetRule;
import ru.example.fintrack.data.entity.Category;
import ru.example.fintrack.data.entity.CategoryLimit;
import ru.example.fintrack.data.entity.FinanceTransaction;
import ru.example.fintrack.data.entity.RecurringPayment;
import ru.example.fintrack.data.relation.AccountWithTransactions;

@Dao
public interface FinanceDao {
    // Справочники
    @Query("SELECT * FROM accounts ORDER BY id LIMIT 1")
    Account getMainAccount();

    @Query("SELECT * FROM categories ORDER BY name")
    LiveData<List<Category>> observeCategories();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    List<Category> getCategories(String type);

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    Category getCategory(long id);

    @Query("SELECT * FROM budget_rules ORDER BY minPercent DESC")
    LiveData<List<BudgetRule>> observeRules();

    @Query("SELECT * FROM budget_rules ORDER BY minPercent DESC")
    List<BudgetRule> getRules();

    // Операции
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    LiveData<List<FinanceTransaction>> observeTransactions();

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    FinanceTransaction getTransaction(long id);

    @Query("SELECT COALESCE(SUM(amountKopecks), 0) FROM transactions " +
            "WHERE categoryId = :categoryId AND type = 'EXPENSE' AND date >= :start AND date < :end")
    long getExpenses(long categoryId, long start, long end);

    @Insert long insertTransaction(FinanceTransaction transaction);
    @Update void updateTransaction(FinanceTransaction transaction);
    @Delete void deleteTransaction(FinanceTransaction transaction);

    // Лимиты
    @Query("SELECT * FROM category_limits WHERE month = :month ORDER BY categoryId")
    LiveData<List<CategoryLimit>> observeLimits(String month);

    @Query("SELECT * FROM category_limits WHERE categoryId = :categoryId AND month = :month LIMIT 1")
    CategoryLimit getLimit(long categoryId, String month);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveLimit(CategoryLimit limit);

    @Query("DELETE FROM category_limits WHERE categoryId = :categoryId AND month = :month")
    void deleteLimit(long categoryId, String month);

    // Регулярные платежи
    @Query("SELECT * FROM recurring_payments ORDER BY dueDate")
    LiveData<List<RecurringPayment>> observePayments();

    @Query("SELECT * FROM recurring_payments WHERE id = :id LIMIT 1")
    RecurringPayment getPayment(long id);

    @Insert long insertPayment(RecurringPayment payment);
    @Update void updatePayment(RecurringPayment payment);
    @Delete void deletePayment(RecurringPayment payment);

    // Демонстрация связанной выборки @Embedded + @Relation.
    @Transaction
    @Query("SELECT * FROM accounts")
    LiveData<List<AccountWithTransactions>> observeAccountsWithTransactions();
}
