package com.example.a1200493_courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.a1200493_courseproject.ui.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataBaseHelper extends android.database.sqlite.SQLiteOpenHelper {

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create table with Email as the primary key
        sqLiteDatabase.execSQL("CREATE TABLE User(" +
                "Email TEXT PRIMARY KEY, " +
                "FirstName TEXT, " +
                "LastName TEXT, " +
                "Password TEXT)");


        sqLiteDatabase.execSQL("CREATE TABLE Tasks(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Title TEXT, " +
                "Description TEXT, " +
                "DueDate TEXT, " +
                "Priority TEXT, " +
                "IsCompleted INTEGER, " +
                "UserEmail TEXT, " +
                "FOREIGN KEY(UserEmail) REFERENCES User(Email) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Tasks");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS User");
        onCreate(sqLiteDatabase);
    }


    public void insertUser(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Email", user.getEmail());
        contentValues.put("FirstName", user.getFirstName());
        contentValues.put("LastName", user.getLastName());
        contentValues.put("Password", user.getPassword());

        // Log the values being inserted
        Log.d("DB_INSERT", "Inserting user: Email=" + user.getEmail()
                + ", FirstName=" + user.getFirstName()
                + ", LastName=" + user.getLastName());

        long result = sqLiteDatabase.insertWithOnConflict("User", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) {
            Log.d("DB_ERROR", "Error inserting data. Email already exists or invalid.");
        } else {
            Log.d("DB_SUCCESS", "Data inserted successfully");
        }
        sqLiteDatabase.close();
    }

    public boolean userExists(String email) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String query = "SELECT * FROM User WHERE Email=?";
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{email});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        sqLiteDatabase.close();
        return exists;
    }

    public boolean isPasswordCorrect(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Password FROM User WHERE Email = ?", new String[]{email});

        boolean isCorrect = false;
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("Password"));
            isCorrect = storedPassword.equals(password);
        }
        cursor.close();
        db.close();
        return isCorrect;
    }


    public void insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Title", task.getTitle());
        values.put("Description", task.getDescription());
        values.put("DueDate", task.getDueDate());
        values.put("Priority", task.getPriority());
        values.put("IsCompleted", task.isCompleted() ? 1 : 0);
        values.put("UserEmail", task.getUserEmail());

        db.insert("Tasks", null, values);
        db.close();
    }

    public void logAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM User";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String email = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow("FirstName"));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow("LastName"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("Password"));

                Log.d("UserTable", "Email: " + email + ", FirstName: " + firstName + ", LastName: " + lastName + ", Password: " + password);
            } while (cursor.moveToNext());
        } else {
            Log.d("UserTable", "No users found.");
        }

        cursor.close();
        db.close();
    }

    public void logAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM Tasks";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("Title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("Description"));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("DueDate"));
                String priority = cursor.getString(cursor.getColumnIndexOrThrow("Priority"));
                boolean isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("IsCompleted")) == 1;
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("UserEmail"));

                Log.d("TasksTable", "ID: " + id + ", Title: " + title + ", Description: " + description +
                        ", DueDate: " + dueDate + ", Priority: " + priority +
                        ", IsCompleted: " + isCompleted + ", UserEmail: " + userEmail);
            } while (cursor.moveToNext());
        } else {
            Log.d("TasksTable", "No tasks found.");
        }

        cursor.close();
        db.close();
    }


    public Map<String, List<Task>> getTasksGroupedByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, List<Task>> groupedTasks = new TreeMap<>(); // TreeMap to ensure chronological order

        Cursor cursor = db.rawQuery("SELECT * FROM Tasks ORDER BY DueDate ASC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("DueDate"));
                Task task = new Task(
                        id, // Include ID
                        cursor.getString(cursor.getColumnIndexOrThrow("Title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("DueDate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Priority")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("IsCompleted")) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow("UserEmail"))
                );

                // Group tasks by date
                if (!groupedTasks.containsKey(dueDate)) {
                    groupedTasks.put(dueDate, new ArrayList<>());
                }
                groupedTasks.get(dueDate).add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return groupedTasks;
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Title", task.getTitle());
        values.put("Description", task.getDescription());
        values.put("DueDate", task.getDueDate());
        values.put("Priority", task.getPriority());
        values.put("IsCompleted", task.isCompleted() ? 1 : 0);

        db.update("Tasks", values, "ID = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Tasks", "ID = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public List<Task> getTasksForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Task> tasksForDate = new ArrayList<>();

        String query = "SELECT * FROM Tasks WHERE DueDate = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                // Create a Task object with all the required fields
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("DueDate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Priority")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("IsCompleted")) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow("UserEmail"))
                );
                tasksForDate.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return tasksForDate;
    }


    public Map<String, List<Task>> getCompletedTasksGroupedByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, List<Task>> groupedTasks = new TreeMap<>(); // TreeMap to ensure chronological order

        // Query to fetch completed tasks sorted by DueDate
        Cursor cursor = db.rawQuery("SELECT * FROM Tasks WHERE IsCompleted = 1 ORDER BY DueDate ASC", null);

        if (cursor.moveToFirst()) {
            do {
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("DueDate"));
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("DueDate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Priority")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("IsCompleted")) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow("UserEmail"))
                );

                // Group tasks by date
                if (!groupedTasks.containsKey(dueDate)) {
                    groupedTasks.put(dueDate, new ArrayList<>());
                }
                groupedTasks.get(dueDate).add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return groupedTasks;
    }


    public boolean updateUser(String oldEmail, String newEmail, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Email", newEmail);
        values.put("Password", newPassword);

        int rowsAffected = db.update("User", values, "Email = ?", new String[]{oldEmail});
        db.close();

        return rowsAffected > 0;
    }

    public List<Task> searchTasksByDateRange(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();

        // Query to fetch tasks between the specified dates
        String query = "SELECT * FROM Tasks WHERE DueDate BETWEEN ? AND ?";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{startDate, endDate});

            // Iterate through the result set
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Title")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                            cursor.getString(cursor.getColumnIndexOrThrow("DueDate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Priority")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("IsCompleted")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("UserEmail"))
                    );
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return tasks;
    }

    public List<Task> searchTasksByKeyword(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();

        // Query to fetch tasks with keyword in title or description
        String query = "SELECT * FROM Tasks WHERE Title LIKE ? OR Description LIKE ?";
        String keywordFilter = "%" + keyword + "%";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{keywordFilter, keywordFilter});

            // Iterate through the result set
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Title")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                            cursor.getString(cursor.getColumnIndexOrThrow("DueDate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Priority")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("IsCompleted")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("UserEmail"))
                    );
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return tasks;
    }


}
