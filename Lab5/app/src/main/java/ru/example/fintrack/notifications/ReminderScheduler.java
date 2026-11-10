package ru.example.fintrack.notifications;

import android.content.Context;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import ru.example.fintrack.data.entity.RecurringPayment;

public final class ReminderScheduler {
    private ReminderScheduler() { }

    public static void checkLimit(Context context, long categoryId, String month) {
        Data data = new Data.Builder()
                .putString("kind", "LIMIT")
                .putLong("categoryId", categoryId)
                .putString("month", month)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(FinanceWorker.class)
                .setInputData(data)
                .setInitialDelay(2, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "limit_" + categoryId + "_" + month, ExistingWorkPolicy.REPLACE, request);
    }

    public static void schedulePayment(Context context, RecurringPayment payment) {
        if (!payment.active) return;
        Data data = new Data.Builder()
                .putString("kind", "PAYMENT")
                .putLong("paymentId", payment.id)
                .build();
        long delay = Math.max(0, payment.dueDate - System.currentTimeMillis());
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(FinanceWorker.class)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("payment_" + payment.id)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "payment_" + payment.id + "_" + payment.dueDate,
                ExistingWorkPolicy.REPLACE, request);
    }

    public static void cancelPayment(Context context, long id) {
        WorkManager.getInstance(context).cancelAllWorkByTag("payment_" + id);
    }
}
