package ru.example.fintrack.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recurring_payments")
public class RecurringPayment {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String title;
    public long amountKopecks;
    public long dueDate;
    public boolean active;

    public RecurringPayment() { this.title = ""; }

    public RecurringPayment(@NonNull String title, long amountKopecks,
                            long dueDate, boolean active) {
        this.title = title;
        this.amountKopecks = amountKopecks;
        this.dueDate = dueDate;
        this.active = active;
    }
}
