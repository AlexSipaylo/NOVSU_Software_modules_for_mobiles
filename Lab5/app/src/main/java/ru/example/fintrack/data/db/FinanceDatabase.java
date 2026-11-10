package ru.example.fintrack.data.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import ru.example.fintrack.data.dao.FinanceDao;
import ru.example.fintrack.data.entity.Account;
import ru.example.fintrack.data.entity.BudgetRule;
import ru.example.fintrack.data.entity.Category;
import ru.example.fintrack.data.entity.CategoryLimit;
import ru.example.fintrack.data.entity.FinanceTransaction;
import ru.example.fintrack.data.entity.RecurringPayment;

@Database(entities = {Account.class, Category.class, FinanceTransaction.class,
        CategoryLimit.class, BudgetRule.class, RecurringPayment.class},
        version = 1, exportSchema = false)
public abstract class FinanceDatabase extends RoomDatabase {
    private static volatile FinanceDatabase INSTANCE;

    public abstract FinanceDao financeDao();

    public static FinanceDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FinanceDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FinanceDatabase.class, "fintrack_simple.db")
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    db.execSQL("INSERT INTO accounts (name) VALUES ('Основной счёт')");

                                    db.execSQL("INSERT INTO categories (name, type) VALUES ('Продукты', 'EXPENSE')");
                                    db.execSQL("INSERT INTO categories (name, type) VALUES ('Транспорт', 'EXPENSE')");
                                    db.execSQL("INSERT INTO categories (name, type) VALUES ('Жильё', 'EXPENSE')");
                                    db.execSQL("INSERT INTO categories (name, type) VALUES ('Развлечения', 'EXPENSE')");
                                    db.execSQL("INSERT INTO categories (name, type) VALUES ('Прочие расходы', 'EXPENSE')");
                                    db.execSQL("INSERT INTO categories (name, type) VALUES ('Зарплата', 'INCOME')");
                                    db.execSQL("INSERT INTO categories (name, type) VALUES ('Прочие доходы', 'INCOME')");

                                    db.execSQL("INSERT INTO budget_rules (minPercent, severity, title, color, icon) VALUES (120, 3, 'Существенное превышение', '#991B1B', '🚨')");
                                    db.execSQL("INSERT INTO budget_rules (minPercent, severity, title, color, icon) VALUES (100, 2, 'Лимит превышен', '#DC2626', '⛔')");
                                    db.execSQL("INSERT INTO budget_rules (minPercent, severity, title, color, icon) VALUES (80, 1, 'Приближение к лимиту', '#D97706', '⚠️')");
                                    db.execSQL("INSERT INTO budget_rules (minPercent, severity, title, color, icon) VALUES (0, 0, 'В пределах нормы', '#16A34A', '✅')");
                                }
                            }).build();
                }
            }
        }
        return INSTANCE;
    }
}
