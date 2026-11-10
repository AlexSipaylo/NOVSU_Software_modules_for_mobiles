package ru.example.fintrack.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;
import ru.example.fintrack.R;
import ru.example.fintrack.data.FinanceRepository;
import ru.example.fintrack.data.entity.Category;
import ru.example.fintrack.data.entity.CategoryLimit;
import ru.example.fintrack.domain.BudgetStatus;
import ru.example.fintrack.util.MoneyUtils;

public class BudgetActivity extends ComponentActivity {
    private FinanceViewModel viewModel;
    private Spinner categorySpinner;
    private EditText limitInput;
    private LinearLayout list;
    private List<Category> expenseCategories = new ArrayList<>();
    private List<CategoryLimit> limits = new ArrayList<>();
    private long selectedCategoryId = -1;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_budget);
        UiUtils.applyInsets(this);
        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        categorySpinner = findViewById(R.id.categorySpinner);
        limitInput = findViewById(R.id.limitInput);
        list = findViewById(R.id.limitList);

        findViewById(R.id.saveLimitButton).setOnClickListener(v -> saveLimit());
        findViewById(R.id.deleteLimitButton).setOnClickListener(v -> confirmDelete());

        viewModel.categories().observe(this, categories -> {
            expenseCategories.clear();
            for (Category category : categories) {
                if ("EXPENSE".equals(category.type)) expenseCategories.add(category);
            }
            categorySpinner.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, expenseCategories));
            selectCurrentCategory();
        });
        viewModel.limits().observe(this, values -> {
            limits = values;
            selectCurrentCategory();
        });
        viewModel.statuses().observe(this, this::render);
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                                                  View view, int position, long id) {
                selectCurrentCategory();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void selectCurrentCategory() {
        if (expenseCategories.isEmpty()) return;
        Category selected = (Category) categorySpinner.getSelectedItem();
        if (selected == null) selected = expenseCategories.get(0);
        selectedCategoryId = selected.id;
        for (CategoryLimit limit : limits) {
            if (limit.categoryId == selectedCategoryId) {
                limitInput.setText(String.format(java.util.Locale.US,
                        "%.2f", limit.limitKopecks / 100.0));
                return;
            }
        }
        limitInput.setText("");
    }

    private void saveLimit() {
        if (selectedCategoryId <= 0) {
            UiUtils.toast(this, "Нет доступных категорий расходов");
            return;
        }
        try {
            long amount = MoneyUtils.parse(limitInput.getText().toString());
            CategoryLimit limit = new CategoryLimit(selectedCategoryId, viewModel.month(), amount);
            viewModel.saveLimit(limit, this::showResult);
        } catch (Exception e) {
            UiUtils.toast(this, "Введите корректную сумму лимита");
        }
    }

    private void confirmDelete() {
        if (selectedCategoryId <= 0) return;
        new AlertDialog.Builder(this).setTitle("Удалить лимит?")
                .setMessage("Лимит выбранной категории будет удалён.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить", (dialog, which) ->
                        viewModel.deleteLimit(selectedCategoryId, this::showResult)).show();
    }

    private void render(List<BudgetStatus> statuses) {
        list.removeAllViews();
        if (statuses == null || statuses.isEmpty()) {
            list.addView(UiUtils.text(this, "Лимиты не заданы. Выберите категорию и укажите сумму.",
                    14, 0xFF64748B, false));
            return;
        }
        for (BudgetStatus status : statuses) {
            int color;
            try { color = Color.parseColor(status.color); }
            catch (Exception e) { color = Color.GRAY; }
            TextView row = UiUtils.text(this,
                    status.icon + "  " + status.categoryName + "\n" + status.title
                            + " · " + status.percent + "%\n"
                            + MoneyUtils.format(status.spent) + " из " + MoneyUtils.format(status.limit),
                    14, color, true);
            row.setPadding(UiUtils.dp(this, 12), UiUtils.dp(this, 10),
                    UiUtils.dp(this, 12), UiUtils.dp(this, 10));
            row.setBackground(UiUtils.roundedBackground(this, 0xFFFFFFFF));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = UiUtils.dp(this, 8);
            row.setLayoutParams(params);
            row.setOnClickListener(v -> {
                for (int i = 0; i < expenseCategories.size(); i++) {
                    if (expenseCategories.get(i).id == status.categoryId) {
                        categorySpinner.setSelection(i);
                        break;
                    }
                }
                selectedCategoryId = status.categoryId;
                for (CategoryLimit limit : limits) {
                    if (limit.categoryId == status.categoryId) {
                        limitInput.setText(String.format(java.util.Locale.US,
                                "%.2f", limit.limitKopecks / 100.0));
                    }
                }
            });
            list.addView(row);
        }
    }

    private void showResult(Void ignored, Exception error) {
        UiUtils.toast(this, error == null ? "Сохранено" : "Ошибка: " + error.getMessage());
    }
}
