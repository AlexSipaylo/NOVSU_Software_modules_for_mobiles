package ru.example.fintrack.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String name;

    public Account() { this.name = ""; }

    public Account(@NonNull String name) {
        this.name = name;
    }
}
