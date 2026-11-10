package ru.example.fintrack.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions",
        foreignKeys = {
                @ForeignKey(entity = Account.class, parentColumns = "id",
                        childColumns = "accountId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Category.class, parentColumns = "id",
                        childColumns = "categoryId", onDelete = ForeignKey.RESTRICT)
        },
        indices = {@Index("accountId"), @Index("categoryId")})
public class FinanceTransaction {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long accountId;
    public long categoryId;
    @NonNull public String type; // EXPENSE или INCOME
    public long amountKopecks;
    @NonNull public String note;
    public long date;

    public FinanceTransaction() { this.type = ""; this.note = ""; }

    public FinanceTransaction(long accountId, long categoryId, @NonNull String type,
                              long amountKopecks, @NonNull String note, long date) {
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.type = type;
        this.amountKopecks = amountKopecks;
        this.note = note;
        this.date = date;
    }
}
