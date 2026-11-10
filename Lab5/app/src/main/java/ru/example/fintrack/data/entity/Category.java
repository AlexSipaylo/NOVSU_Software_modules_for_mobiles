package ru.example.fintrack.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String name;
    @NonNull public String type; // EXPENSE или INCOME

    public Category() { this.name = ""; this.type = ""; }

    public Category(@NonNull String name, @NonNull String type) {
        this.name = name;
        this.type = type;
    }

    @NonNull @Override public String toString() { return name; }
}
