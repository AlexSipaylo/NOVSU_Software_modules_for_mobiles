package ru.example.fintrack.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;
import java.util.Calendar;
import java.util.List;
import ru.example.fintrack.R;
import ru.example.fintrack.data.entity.RecurringPayment;
import ru.example.fintrack.util.DateUtils;
import ru.example.fintrack.util.MoneyUtils;

public class PaymentsActivity extends ComponentActivity {
    private FinanceViewModel viewModel;
    private LinearLayout list;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_payments);
        UiUtils.applyInsets(this);
        viewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        list = findViewById(R.id.paymentList);
        findViewById(R.id.addPaymentButton).setOnClickListener(v -> showPaymentForm());
        viewModel.payments().observe(this, this::render);
    }

    private void showPaymentForm() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(UiUtils.dp(this, 20), UiUtils.dp(this, 8),
                UiUtils.dp(this, 20), 0);

        EditText title = new EditText(this);
        title.setHint("Название платежа, например Интернет");
        form.addView(title);

        EditText amount = new EditText(this);
        amount.setHint("Сумма, ₽");
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        form.addView(amount);

        Calendar dueDate = Calendar.getInstance();
        dueDate.add(Calendar.DAY_OF_MONTH, 1);
        dueDate.set(Calendar.HOUR_OF_DAY, 9);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        Button chooseDate = new Button(this);
        chooseDate.setText("Дата первого напоминания: " + DateUtils.format(dueDate.getTimeInMillis()));
        form.addView(chooseDate);
        chooseDate.setOnClickListener(v -> new DatePickerDialog(this, (picker, year, month, day) -> {
            dueDate.set(year, month, day, 9, 0, 0);
            chooseDate.setText("Дата первого напоминания: " + DateUtils.format(dueDate.getTimeInMillis()));
        }, dueDate.get(Calendar.YEAR), dueDate.get(Calendar.MONTH),
                dueDate.get(Calendar.DAY_OF_MONTH)).show());

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Ежемесячный платёж")
                .setView(form).setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    try {
                        String name = title.getText().toString().trim();
                        if (name.isEmpty()) throw new IllegalArgumentException();
                        long cents = MoneyUtils.parse(amount.getText().toString());
                        RecurringPayment payment = new RecurringPayment(name, cents,
                                dueDate.getTimeInMillis(), true);
                        viewModel.addPayment(payment, this::showResult);
                        dialog.dismiss();
                    } catch (Exception e) {
                        UiUtils.toast(this, "Введите название и корректную сумму");
                    }
                }));
        dialog.show();
    }

    private void render(List<RecurringPayment> payments) {
        list.removeAllViews();
        if (payments == null || payments.isEmpty()) {
            list.addView(UiUtils.text(this,
                    "Регулярных платежей пока нет. Добавьте платёж, чтобы настроить напоминание.",
                    14, 0xFF64748B, false));
            return;
        }
        for (RecurringPayment payment : payments) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(UiUtils.dp(this, 12), UiUtils.dp(this, 10),
                    UiUtils.dp(this, 12), UiUtils.dp(this, 10));
            row.setBackground(UiUtils.roundedBackground(this, 0xFFFFFFFF));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = UiUtils.dp(this, 8);
            row.setLayoutParams(params);
            row.addView(UiUtils.text(this, payment.title, 15, 0xFF1F2937, true));
            row.addView(UiUtils.text(this, MoneyUtils.format(payment.amountKopecks)
                    + " · следующее напоминание " + DateUtils.format(payment.dueDate)
                    + " · повтор каждый месяц", 13, 0xFF64748B, false));
            Button delete = new Button(this);
            delete.setText("Удалить напоминание");
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Удалить платёж?")
                    .setMessage("Будущие напоминания также будут отменены.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить", (dialog, which) ->
                            viewModel.deletePayment(payment.id, this::showResult)).show());
            row.addView(delete);
            list.addView(row);
        }
    }

    private void showResult(Void ignored, Exception error) {
        UiUtils.toast(this, error == null ? "Сохранено" : "Ошибка: " + error.getMessage());
    }
}
