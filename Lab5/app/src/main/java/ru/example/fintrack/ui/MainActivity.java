package ru.example.fintrack.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import ru.example.fintrack.R;
import ru.example.fintrack.data.FinanceRepository;
import ru.example.fintrack.data.entity.Category;
import ru.example.fintrack.data.entity.FinanceTransaction;
import ru.example.fintrack.domain.BudgetStatus;
import ru.example.fintrack.util.DateUtils;
import ru.example.fintrack.util.MoneyUtils;

public class MainActivity extends ComponentActivity {
    private FinanceViewModel viewModel;
    private LinearLayout transactionsBox, budgetsBox;
    private TextView emptyText, balanceText, incomeText, expenseText;
    private List<Category> categories = new ArrayList<>();
    private List<FinanceTransaction> transactions = new ArrayList<>();
    private ActivityResultLauncher<String> permissionLauncher;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        UiUtils.applyInsets(this);

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) UiUtils.toast(this,
                            "Уведомления отключены. Учёт финансов продолжит работать.");
                });
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        transactionsBox = findViewById(R.id.transactionsBox);
        budgetsBox = findViewById(R.id.budgetsBox);
        emptyText = findViewById(R.id.emptyText);
        balanceText = findViewById(R.id.balanceText);
        incomeText = findViewById(R.id.incomeText);
        expenseText = findViewById(R.id.expenseText);

        findViewById(R.id.addButton).setOnClickListener(v -> showTransactionForm(null));
        findViewById(R.id.budgetsButton).setOnClickListener(v ->
                startActivity(new Intent(this, BudgetActivity.class)));
        findViewById(R.id.paymentsButton).setOnClickListener(v ->
                startActivity(new Intent(this, PaymentsActivity.class)));

        viewModel.categories().observe(this, list -> {
            categories = list;
            renderTransactions();
        });
        viewModel.transactions().observe(this, list -> {
            transactions = list;
            renderSummary();
            renderTransactions();
        });
        viewModel.statuses().observe(this, this::renderBudgets);
        openScreenFromIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openScreenFromIntent(intent);
    }

    private void openScreenFromIntent(Intent intent) {
        if (intent == null) return;
        String screen = intent.getStringExtra("screen");
        if ("BUDGET".equals(screen)) startActivity(new Intent(this, BudgetActivity.class));
        if ("PAYMENTS".equals(screen)) startActivity(new Intent(this, PaymentsActivity.class));
    }

    private void renderSummary() {
        long balance = 0, income = 0, expense = 0;
        long start = DateUtils.monthStart(System.currentTimeMillis());
        long end = DateUtils.nextMonthStart(System.currentTimeMillis());
        for (FinanceTransaction item : transactions) {
            if ("INCOME".equals(item.type)) balance += item.amountKopecks;
            else balance -= item.amountKopecks;
            if (item.date >= start && item.date < end) {
                if ("INCOME".equals(item.type)) income += item.amountKopecks;
                else expense += item.amountKopecks;
            }
        }
        balanceText.setText(MoneyUtils.format(balance));
        incomeText.setText(MoneyUtils.format(income));
        expenseText.setText(MoneyUtils.format(expense));
    }

    private void renderTransactions() {
        transactionsBox.removeAllViews();
        if (transactions.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        for (FinanceTransaction item : transactions) {
            Category category = findCategory(item.categoryId);
            String categoryName = category == null ? "Категория" : category.name;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(UiUtils.dp(this, 14), UiUtils.dp(this, 10),
                    UiUtils.dp(this, 14), UiUtils.dp(this, 10));
            row.setBackground(UiUtils.roundedBackground(this, Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = UiUtils.dp(this, 8);
            row.setLayoutParams(params);
            row.addView(UiUtils.text(this, categoryName + " · " + DateUtils.format(item.date),
                    15, Color.rgb(31, 41, 55), true));
            boolean isIncome = "INCOME".equals(item.type);
            row.addView(UiUtils.text(this,
                    (isIncome ? "+ " : "− ") + MoneyUtils.format(item.amountKopecks)
                            + (item.note.isEmpty() ? "" : " · " + item.note),
                    14, isIncome ? Color.rgb(22, 163, 74) : Color.rgb(220, 38, 38), true));
            row.setOnClickListener(v -> showTransactionDetails(item));
            transactionsBox.addView(row);
        }
    }

    private void renderBudgets(List<BudgetStatus> statuses) {
        budgetsBox.removeAllViews();
        if (statuses == null || statuses.isEmpty()) {
            budgetsBox.addView(UiUtils.text(this,
                    "Лимиты ещё не заданы. Откройте раздел «Лимиты», чтобы добавить первый.",
                    14, Color.GRAY, false));
            return;
        }
        for (BudgetStatus status : statuses) {
            int color;
            try { color = Color.parseColor(status.color); }
            catch (Exception e) { color = Color.GRAY; }
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(UiUtils.dp(this, 12), UiUtils.dp(this, 10),
                    UiUtils.dp(this, 12), UiUtils.dp(this, 10));
            card.setBackground(UiUtils.roundedBackground(this, Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = UiUtils.dp(this, 8);
            card.setLayoutParams(params);
            card.addView(UiUtils.text(this, status.icon + "  " + status.categoryName + " — " + status.title,
                    14, color, true));
            card.addView(UiUtils.text(this,
                    MoneyUtils.format(status.spent) + " из " + MoneyUtils.format(status.limit)
                            + " (" + status.percent + "%)", 13, Color.DKGRAY, false));
            String difference = status.remaining >= 0 ? "Остаток: " : "Перерасход: ";
            card.addView(UiUtils.text(this, difference + MoneyUtils.format(Math.abs(status.remaining)),
                    13, color, true));
            budgetsBox.addView(card);
        }
    }

    private Category findCategory(long id) {
        for (Category category : categories) if (category.id == id) return category;
        return null;
    }

    private void showTransactionDetails(FinanceTransaction item) {
        Category category = findCategory(item.categoryId);
        String type = "INCOME".equals(item.type) ? "Доход" : "Расход";
        String message = type + "\nКатегория: " + (category == null ? "—" : category.name)
                + "\nСумма: " + MoneyUtils.format(item.amountKopecks)
                + "\nДата: " + DateUtils.format(item.date)
                + "\nКомментарий: " + item.note;
        new AlertDialog.Builder(this).setTitle("Операция").setMessage(message)
                .setNeutralButton("Изменить", (dialog, which) -> showTransactionForm(item))
                .setNegativeButton("Удалить", (dialog, which) -> confirmDelete(item))
                .setPositiveButton("Закрыть", null).show();
    }

    private void confirmDelete(FinanceTransaction item) {
        new AlertDialog.Builder(this).setTitle("Удалить операцию?")
                .setMessage("Запись будет удалена из истории и лимит пересчитается.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить", (dialog, which) ->
                        viewModel.deleteTransaction(item.id, this::showResult)).show();
    }

    private void showTransactionForm(FinanceTransaction old) {
        if (categories.isEmpty()) {
            UiUtils.toast(this, "Категории ещё загружаются");
            return;
        }
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(UiUtils.dp(this, 20), UiUtils.dp(this, 8),
                UiUtils.dp(this, 20), 0);

        Spinner typeSpinner = new Spinner(this);
        String[] typeNames = {"Расход", "Доход"};
        typeSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, typeNames));
        typeSpinner.setSelection(old != null && "INCOME".equals(old.type) ? 1 : 0);
        form.addView(typeSpinner);

        Spinner categorySpinner = new Spinner(this);
        form.addView(categorySpinner);
        final List<Category>[] visibleCategories = new List[]{new ArrayList<>()};
        Runnable refreshCategories = () -> {
            String type = typeSpinner.getSelectedItemPosition() == 0 ? "EXPENSE" : "INCOME";
            visibleCategories[0] = new ArrayList<>();
            for (Category category : categories) if (type.equals(category.type)) visibleCategories[0].add(category);
            categorySpinner.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, visibleCategories[0]));
            if (old != null) {
                for (int i = 0; i < visibleCategories[0].size(); i++) {
                    if (visibleCategories[0].get(i).id == old.categoryId) categorySpinner.setSelection(i);
                }
            }
        };
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshCategories.run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        EditText amountInput = new EditText(this);
        amountInput.setHint("Сумма, ₽");
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (old != null) amountInput.setText(String.format(java.util.Locale.US,
                "%.2f", old.amountKopecks / 100.0));
        form.addView(amountInput);

        EditText noteInput = new EditText(this);
        noteInput.setHint("Комментарий (необязательно)");
        if (old != null) noteInput.setText(old.note);
        form.addView(noteInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(old == null ? "Новая операция" : "Изменить операцию")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    try {
                        if (visibleCategories[0].isEmpty()) throw new IllegalArgumentException("Нет категории");
                        long amount = MoneyUtils.parse(amountInput.getText().toString());
                        Category category = visibleCategories[0].get(categorySpinner.getSelectedItemPosition());
                        String type = typeSpinner.getSelectedItemPosition() == 0 ? "EXPENSE" : "INCOME";
                        long date = old == null ? System.currentTimeMillis() : old.date;
                        FinanceTransaction item = new FinanceTransaction(1, category.id, type,
                                amount, noteInput.getText().toString().trim(), date);
                        boolean isNew = old == null;
                        if (!isNew) item.id = old.id;
                        viewModel.saveTransaction(item, isNew, this::showResult);
                        dialog.dismiss();
                    } catch (Exception e) {
                        UiUtils.toast(this, "Введите корректную сумму и выберите категорию");
                    }
                }));
        dialog.show();
    }

    private void showResult(Void ignored, Exception error) {
        UiUtils.toast(this, error == null ? "Сохранено" : "Ошибка: " + error.getMessage());
    }
}
