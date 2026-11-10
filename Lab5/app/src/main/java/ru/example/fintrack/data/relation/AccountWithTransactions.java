package ru.example.fintrack.data.relation;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;
import ru.example.fintrack.data.entity.Account;
import ru.example.fintrack.data.entity.FinanceTransaction;

// Один объект счёта вместе со всеми его операциями.
public class AccountWithTransactions {
    @Embedded public Account account;

    @Relation(parentColumn = "id", entityColumn = "accountId")
    public List<FinanceTransaction> transactions;
}
