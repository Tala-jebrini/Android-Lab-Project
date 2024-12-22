package com.example.a1200493_courseproject.ui;

public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String priority;
    private boolean isCompleted;
    private String userEmail;

    public Task(){

    }
    // Constructor with ID
    public Task(int id, String title, String description, String dueDate, String priority, boolean isCompleted, String userEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.userEmail = userEmail;
    }


    public Task(String title, String description, String dueDate, String priority, boolean isCompleted, String userEmail) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.userEmail = userEmail;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
