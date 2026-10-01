package com.example.clinicappointment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextInputLayout fioLayout;
    private TextInputLayout birthDateLayout;
    private TextInputLayout omsLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout specializationLayout;
    private TextInputLayout appointmentDateLayout;
    private TextInputLayout appointmentTimeLayout;
    private TextInputLayout complaintsLayout;
    private TextInputEditText fioEdit;
    private TextInputEditText birthDateEdit;
    private TextInputEditText omsEdit;
    private TextInputEditText phoneEdit;
    private TextInputEditText appointmentDateEdit;
    private TextInputEditText appointmentTimeEdit;
    private TextInputEditText complaintsEdit;
    private AutoCompleteTextView specializationSpinner;
    private Button submitButton;
    private Button calendarButton;
    private SharedPreferences preferences;
    private final Calendar appointmentCalendar = Calendar.getInstance();

    private boolean formSubmitted = false;
    private boolean restoringDraft = false;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            showSnackbar("Уведомления разрешены");
                        } else {
                            showSnackbar("Уведомления отключены. " + "Вы всё равно можете записаться на приём.");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupSpecializations();
        preferences = getSharedPreferences("clinic_preferences", MODE_PRIVATE);
        restoreDraft();
        setupListeners();
        validateForm();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveDraft();
    }

    private void initViews() {
        fioLayout = findViewById(R.id.fioLayout);
        birthDateLayout = findViewById(R.id.birthDateLayout);
        omsLayout = findViewById(R.id.omsLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        specializationLayout = findViewById(R.id.specializationLayout);
        appointmentDateLayout = findViewById(R.id.appointmentDateLayout);
        appointmentTimeLayout = findViewById(R.id.appointmentTimeLayout);
        complaintsLayout = findViewById(R.id.complaintsLayout);
        fioEdit = findViewById(R.id.fioEdit);
        birthDateEdit = findViewById(R.id.birthDateEdit);
        omsEdit = findViewById(R.id.omsEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
        appointmentDateEdit = findViewById(R.id.appointmentDateEdit);
        appointmentTimeEdit = findViewById(R.id.appointmentTimeEdit);
        complaintsEdit = findViewById(R.id.complaintsEdit);
        specializationSpinner = findViewById(R.id.specializationSpinner);
        submitButton = findViewById(R.id.submitButton);
        calendarButton = findViewById(R.id.calendarButton);
    }

    private void setupSpecializations() {
        String[] specializations = {"Терапевт", "Кардиолог", "Невролог", "Офтальмолог", "ЛОР", "Дерматолог", "Хирург", "Стоматолог"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, specializations);
        specializationSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        fioEdit.addTextChangedListener(simpleWatcher);
        birthDateEdit.addTextChangedListener(simpleWatcher);
        omsEdit.addTextChangedListener(simpleWatcher);
        phoneEdit.addTextChangedListener(simpleWatcher);
        appointmentDateEdit.addTextChangedListener(simpleWatcher);
        appointmentTimeEdit.addTextChangedListener(simpleWatcher);
        complaintsEdit.addTextChangedListener(simpleWatcher);
        specializationSpinner.setOnItemClickListener((parent, view, position, id) -> {
            saveDraft();
            validateForm();
        });
        birthDateEdit.setOnClickListener(v -> showBirthDatePicker());
        appointmentDateEdit.setOnClickListener(v -> showAppointmentDatePicker());
        appointmentTimeEdit.setOnClickListener(v -> showAppointmentTimePicker());
        submitButton.setOnClickListener(v -> {
            formSubmitted = true;
            if (validateForm()) {
                showConfirmationDialog();
            }
        });
        calendarButton.setOnClickListener(v -> addAppointmentToCalendar());
    }

    private final TextWatcher simpleWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!restoringDraft) {
                saveDraft();
            }
            validateForm();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private boolean validateForm() {
        boolean ok = true;
        String fio = getText(fioEdit);
        String birthDate = getText(birthDateEdit);
        String oms = getText(omsEdit);
        String phone = getText(phoneEdit);
        String specialization = specializationSpinner.getText().toString().trim();
        String appointmentDate = getText(appointmentDateEdit);
        String appointmentTime = getText(appointmentTimeEdit);
        String complaints = getText(complaintsEdit);

        if (fio.isEmpty()) {
            if (formSubmitted) {
                fioLayout.setError("Введите ФИО");
            }
            ok = false;
        } else if (fio.length() < 5) {
            if (formSubmitted) {
                fioLayout.setError("Введите полное ФИО");
            }
            ok = false;
        } else {
            fioLayout.setError(null);
        }

        if (birthDate.isEmpty()) {
            if (formSubmitted) {
                birthDateLayout.setError("Выберите дату рождения");
            }
            ok = false;
        } else {
            birthDateLayout.setError(null);
        }

        if (oms.isEmpty()) {
            if (formSubmitted) {
                omsLayout.setError("Введите номер полиса ОМС");
            }
            ok = false;
        } else if (!oms.matches("\\d{16}")) {
            if (formSubmitted) {
                omsLayout.setError("Полис ОМС должен содержать 16 цифр");
            }
            ok = false;
        } else {
            omsLayout.setError(null);
        }

        if (phone.isEmpty()) {
            if (formSubmitted) {
                phoneLayout.setError("Введите номер телефона");
            }
            ok = false;
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            if (formSubmitted) {
                phoneLayout.setError("Введите корректный номер телефона");
            }
            ok = false;
        } else {
            phoneLayout.setError(null);
        }

        if (specialization.isEmpty()) {
            if (formSubmitted) {
                specializationLayout.setError("Выберите специализацию врача");
            }
            ok = false;
        } else {
            specializationLayout.setError(null);
        }

        if (appointmentDate.isEmpty()) {
            if (formSubmitted) {
                appointmentDateLayout.setError("Выберите дату приёма");
            }
            ok = false;
        } else {
            appointmentDateLayout.setError(null);
        }

        if (appointmentTime.isEmpty()) {
            if (formSubmitted) {
                appointmentTimeLayout.setError("Выберите время приёма");
            }
            ok = false;
        } else {
            appointmentTimeLayout.setError(null);
        }

        if (complaints.isEmpty()) {
            if (formSubmitted) {
                complaintsLayout.setError("Опишите причину обращения");
            }
            ok = false;
        } else if (complaints.length() < 5) {
            if (formSubmitted) {
                complaintsLayout.setError("Опишите жалобы подробнее");
            }
            ok = false;
        } else {
            complaintsLayout.setError(null);
        }

        submitButton.setEnabled(ok);
        return ok;
    }

    private void showBirthDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(),
                            "%02d.%02d.%04d", dayOfMonth, month + 1, year);
                    birthDateEdit.setText(date);
                }, 2000, 0, 1);

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showAppointmentDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    appointmentCalendar.set(Calendar.YEAR, year);
                    appointmentCalendar.set(Calendar.MONTH, month);
                    appointmentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String date = String.format(Locale.getDefault(), "%02d.%02d.%04d", dayOfMonth, month + 1, year);
                    appointmentDateEdit.setText(date);
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showAppointmentTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    appointmentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    appointmentCalendar.set(Calendar.MINUTE, minute);
                    appointmentCalendar.set(Calendar.SECOND, 0);
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    appointmentTimeEdit.setText(time);
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);

        dialog.show();
    }

    private void showConfirmationDialog() {
        String fio = getText(fioEdit);
        String specialization = specializationSpinner.getText().toString().trim();
        String date = getText(appointmentDateEdit);
        String time = getText(appointmentTimeEdit);
        String message = "Пациент: " + fio + "\nВрач: " + specialization
                + "\nДата: " + date + "\nВремя: " + time;

        new MaterialAlertDialogBuilder(this).setTitle("Подтверждение записи")
                .setMessage(message).setNegativeButton("Отмена", null)
                .setPositiveButton("Записаться", (dialog, which) -> {
                    saveDraft();
                    Toast.makeText(this, "Запись успешно создана", Toast.LENGTH_LONG).show();
                    calendarButton.setVisibility(View.VISIBLE);
                    requestNotificationPermission();
                }).show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                new MaterialAlertDialogBuilder(this).setTitle("Разрешение на уведомления")
                        .setMessage("Разрешение необходимо, чтобы приложение могло напоминать о предстоящем приёме.")
                        .setNegativeButton("Не сейчас", (dialog, which) ->
                                showSnackbar("Вы можете продолжить без уведомлений"))
                        .setPositiveButton("Разрешить", (dialog, which) ->
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .show();
            } else {
                showSnackbar("Уведомления уже разрешены");
            }
        } else {
            showSnackbar("На этой версии Android отдельное разрешение не требуется");
        }
    }

    private void addAppointmentToCalendar() {
        if (!validateForm()) {
            formSubmitted = true;
            validateForm();
            return;
        }

        restoreAppointmentDate();
        restoreAppointmentTime();

        String fio = getText(fioEdit);
        String specialization = specializationSpinner.getText().toString().trim();
        String complaints = getText(complaintsEdit);
        String title = "Приём врача";
        String description = "Пациент: " + fio + "\n" + "Специализация: " + specialization + "\n" +
                "Жалобы: " + complaints + "\n" + "Адрес клиники: г. Великий Новгород, ул. Ломоносова, 25";

        long beginMs = appointmentCalendar.getTimeInMillis();
        long endMs = beginMs + 60 * 60 * 1000;

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "г. Великий Новгород, ул. Ломоносова, 25");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMs);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs);
        intent.putExtra(CalendarContract.Events.HAS_ALARM, 1);

        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar("Не удалось открыть календарь");
        }
    }

    private void saveDraft() {
        if (preferences == null) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("fio", getText(fioEdit));
        editor.putString("birth_date", getText(birthDateEdit));
        editor.putString("oms", getText(omsEdit));
        editor.putString("phone", getText(phoneEdit));
        editor.putString("specialization", specializationSpinner.getText().toString().trim());
        editor.putString("appointment_date", getText(appointmentDateEdit));
        editor.putString("appointment_time", getText(appointmentTimeEdit));
        editor.putString("complaints", getText(complaintsEdit));

        editor.apply();
    }

    private void restoreDraft() {
        restoringDraft = true;

        fioEdit.setText(preferences.getString("fio", ""));
        birthDateEdit.setText(preferences.getString("birth_date", ""));
        omsEdit.setText(preferences.getString("oms", ""));
        phoneEdit.setText(preferences.getString("phone", ""));
        specializationSpinner.setText(preferences.getString("specialization", ""), false);
        appointmentDateEdit.setText(preferences.getString("appointment_date", ""));
        appointmentTimeEdit.setText(preferences.getString("appointment_time", ""));
        complaintsEdit.setText(preferences.getString("complaints", ""));

        restoreAppointmentDate();
        restoreAppointmentTime();

        restoringDraft = false;
    }

    private void restoreAppointmentDate() {
        try {
            String date = getText(appointmentDateEdit);
            if (date.isEmpty()) {
                return;
            }
            String[] parts = date.split("\\.");
            if (parts.length != 3) {
                return;
            }
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);

            appointmentCalendar.set(Calendar.YEAR, year);
            appointmentCalendar.set(Calendar.MONTH, month);
            appointmentCalendar.set(Calendar.DAY_OF_MONTH, day);
        } catch (Exception ignored) {
        }
    }

    private void restoreAppointmentTime() {
        try {
            String time = getText(appointmentTimeEdit);
            if (time.isEmpty()) {
                return;
            }
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return;
            }
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            appointmentCalendar.set(Calendar.HOUR_OF_DAY, hour);
            appointmentCalendar.set(Calendar.MINUTE, minute);
            appointmentCalendar.set(Calendar.SECOND, 0);
        } catch (Exception ignored) {
        }
    }

    private String getText(TextInputEditText editText) {
        if (editText == null) {
            return "";
        }
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void showSnackbar(String message) {
        View root = findViewById(android.R.id.content);
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }
}
