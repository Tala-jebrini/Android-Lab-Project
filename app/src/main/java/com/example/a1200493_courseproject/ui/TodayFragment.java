package com.example.a1200493_courseproject.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.a1200493_courseproject.DataBaseHelper;
import com.example.a1200493_courseproject.HttpManager;
import com.example.a1200493_courseproject.R;
import com.example.a1200493_courseproject.ui.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TodayFragment extends Fragment {

    private DataBaseHelper dataBaseHelper;
    private LinearLayout taskContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_today, container, false);

        // Initialize database helper
        dataBaseHelper = new DataBaseHelper(getContext(), "User", null, 2);

        // Get today's date in the correct format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // Fetch tasks for today
        List<Task> todayTasks = dataBaseHelper.getTasksForDate(todayDate);

        // Get the container layout
        taskContainer = root.findViewById(R.id.task_container);

        // Populate tasks
        populateTasks(todayTasks);

        // Set up "Import Tasks" button
        Button importTasksButton = root.findViewById(R.id.import_tasks_button);
        if (importTasksButton != null) {
            importTasksButton.setOnClickListener(v -> {
                new ImportTasksAsyncTask().execute("https://mocki.io/v1/5faa65cc-deac-41b7-aecf-be507188f730");
            });
        } else {
            Log.e("TodayFragment", "import_tasks_button is null!");
        }

        return root;
    }


    private void populateTasks(List<Task> tasks) {
        // Clear existing views
        taskContainer.removeAllViews();

        if (tasks.isEmpty()) {
            TextView noTasksTextView = new TextView(getContext());
            noTasksTextView.setText("No tasks for today.");
            noTasksTextView.setTextSize(16);
            noTasksTextView.setPadding(16, 16, 16, 16);
            taskContainer.addView(noTasksTextView);
        } else {
            for (Task task : tasks) {
                // Create a layout for each task
                LinearLayout taskLayout = new LinearLayout(getContext());
                taskLayout.setOrientation(LinearLayout.HORIZONTAL);
                taskLayout.setPadding(16, 8, 8, 8);

                // Task details
                TextView taskTextView = new TextView(getContext());
                String taskDetails = "• " + task.getTitle() +
                        "\nDescription: " + task.getDescription() +
                        "\nPriority: " + task.getPriority() +
                        "\nCompleted: " + (task.isCompleted() ? "Yes" : "No");
                taskTextView.setText(taskDetails);
                taskTextView.setTextSize(16);
                taskTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                taskLayout.addView(taskTextView);

                // Add edit icon
                ImageView editIcon = new ImageView(getContext());
                editIcon.setImageResource(R.drawable.ic_baseline_edit_note_24);
                editIcon.setPadding(8, 0, 8, 0);
                taskLayout.addView(editIcon);

                // Add delete icon
                ImageView deleteIcon = new ImageView(getContext());
                deleteIcon.setImageResource(R.drawable.ic_baseline_delete_24);
                deleteIcon.setPadding(8, 0, 8, 0);
                taskLayout.addView(deleteIcon);

                // Add share icon
                ImageView shareIcon = new ImageView(getContext());
                shareIcon.setImageResource(R.drawable.ic_baseline_ios_share_24);
                shareIcon.setPadding(8, 0, 8, 0);
                taskLayout.addView(shareIcon);

                // Set click listeners
                editIcon.setOnClickListener(v -> showEditDialog(task));
                deleteIcon.setOnClickListener(v -> showDeleteConfirmationDialog(task.getId()));
                shareIcon.setOnClickListener(v -> shareTaskViaEmail(task));

                taskContainer.addView(taskLayout);
            }
        }
    }

    private class ImportTasksAsyncTask extends AsyncTask<String, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(String... urls) {
            String jsonResponse = HttpManager.getData(urls[0]);
            return parseTasksFromJson(jsonResponse);
        }

        @Override
        protected void onPostExecute(List<Task> tasks) {
            if (tasks == null) {
                Toast.makeText(getContext(), "Failed to import tasks.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert tasks into the database for today's date
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            for (Task task : tasks) {
                task.setDueDate(todayDate); // Assign today's date
                dataBaseHelper.insertTask(task);
            }

            // Refresh the task list
            List<Task> updatedTasks = dataBaseHelper.getTasksForDate(todayDate);
            populateTasks(updatedTasks);

            Toast.makeText(getContext(), "Tasks imported successfully!", Toast.LENGTH_SHORT).show();
        }

        private List<Task> parseTasksFromJson(String jsonResponse) {
            List<Task> tasks = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(jsonResponse);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Task task = new Task();
                    task.setTitle(jsonObject.getString("title"));
                    task.setDescription(jsonObject.getString("description"));
                    task.setPriority(jsonObject.getString("priority"));
                    task.setCompleted(jsonObject.getBoolean("isCompleted"));
                    tasks.add(task);
                }
            } catch (JSONException e) {
                Log.e("TodayFragment", "Error parsing JSON", e);
                return null;
            }
            return tasks;
        }
    }

    private void refreshTasks() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        List<Task> todayTasks = dataBaseHelper.getTasksForDate(todayDate);
        populateTasks(todayTasks);
    }

    private void showEditDialog(Task task) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_task, null);

        EditText taskTitle = dialogView.findViewById(R.id.edit_task_title);
        EditText taskDescription = dialogView.findViewById(R.id.edit_task_description);
        Button dueDateButton = dialogView.findViewById(R.id.edit_select_due_date);
        Spinner prioritySpinner = dialogView.findViewById(R.id.edit_priority_spinner);
        CheckBox completedCheckbox = dialogView.findViewById(R.id.edit_completed_checkbox);

        // Populate the fields with task details
        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());
        dueDateButton.setText(task.getDueDate());
        completedCheckbox.setChecked(task.isCompleted());

        // Handle date picker for due date
        dueDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
                String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                dueDateButton.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Task");
        builder.setView(dialogView);
        builder.setPositiveButton("Update", (dialog, which) -> {
            task.setTitle(taskTitle.getText().toString().trim());
            task.setDescription(taskDescription.getText().toString().trim());
            task.setDueDate(dueDateButton.getText().toString().trim());
            task.setPriority(prioritySpinner.getSelectedItem().toString());
            task.setCompleted(completedCheckbox.isChecked());

            // Update task in the database
            dataBaseHelper.updateTask(task);

            // Refresh the task list
            refreshTasks();

            Toast.makeText(getContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    private void showDeleteConfirmationDialog(int taskId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dataBaseHelper.deleteTask(taskId); // Delete task from database
            refreshTasks(); // Refresh the task list
            Toast.makeText(getContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    private void shareTaskViaEmail(Task task) {
        String subject = "Task: " + task.getTitle();
        String message = "Task Details:\n" +
                "Title: " + task.getTitle() + "\n" +
                "Description: " + task.getDescription() + "\n" +
                "Due Date: " + task.getDueDate() + "\n" +
                "Priority: " + task.getPriority() + "\n" +
                "Completed: " + (task.isCompleted() ? "Yes" : "No");

        String mailto = "mailto:" +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(message);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
