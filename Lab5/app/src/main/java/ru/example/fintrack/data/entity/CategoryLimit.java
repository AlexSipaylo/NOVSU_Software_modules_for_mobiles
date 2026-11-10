package ru.example.fintrack.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "category_limits",
        foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id",
                childColumns = "categoryId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("categoryId"), @Index(value = {"categoryId", "month"}, unique = true)})
public class CategoryLimit {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long categoryId;
    public String month; // yyyy-MM
    public long limitKopecks;

    public CategoryLimit() { this.month = ""; }

    public CategoryLimit(long categoryId, String month, long limitKopecks) {
        this.categoryId = categoryId;
        this.month = month;
        this.limitKopecks = limitKopecks;
    }
}
