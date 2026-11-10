package ru.example.fintrack;

import android.app.Application;
import ru.example.fintrack.notifications.NotificationHelper;

public class FinanceApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannel(this);
    }
}
