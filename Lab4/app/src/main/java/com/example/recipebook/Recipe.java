package com.example.recipebook;

public class Recipe {
    private long id;
    private String title;
    private String category;
    private int prepMinutes;
    private String ingredients;
    private String steps;
    private String imagePath;
    private Long calendarEventId;
    private Long plannedAt;
    private boolean weeklyRepeat;

    public Recipe(long id, String title, String category, int prepMinutes,
                  String ingredients, String steps, String imagePath,
                  Long calendarEventId, Long plannedAt, boolean weeklyRepeat) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.prepMinutes = prepMinutes;
        this.ingredients = ingredients;
        this.steps = steps;
        this.imagePath = imagePath;
        this.calendarEventId = calendarEventId;
        this.plannedAt = plannedAt;
        this.weeklyRepeat = weeklyRepeat;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public int getPrepMinutes() { return prepMinutes; }
    public String getIngredients() { return ingredients; }
    public String getSteps() { return steps; }
    public String getImagePath() { return imagePath; }
    public Long getCalendarEventId() { return calendarEventId; }
    public Long getPlannedAt() { return plannedAt; }
    public boolean isWeeklyRepeat() { return weeklyRepeat; }

    public void setId(long id) { this.id = id; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setCalendarEventId(Long calendarEventId) { this.calendarEventId = calendarEventId; }
    public void setPlannedAt(Long plannedAt) { this.plannedAt = plannedAt; }
    public void setWeeklyRepeat(boolean weeklyRepeat) { this.weeklyRepeat = weeklyRepeat; }
}
