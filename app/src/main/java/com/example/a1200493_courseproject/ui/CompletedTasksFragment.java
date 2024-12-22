package com.example.a1200493_courseproject.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.a1200493_courseproject.R;
import com.example.a1200493_courseproject.ui.Task;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CompletedTasksFragment extends Fragment {

    private DataBaseHelper dataBaseHelper;
    private LinearLayout taskContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_completed, container, false);

        // Initialize database helper
        dataBaseHelper = new DataBaseHelper(getContext(), "User", null, 2);

        // Get the container layout
        taskContainer = root.findViewById(R.id.task_container);

        // Load tasks
        loadCompletedTasks();

        return root;
    }

    private void loadCompletedTasks() {
        // Clear existing views
        taskContainer.removeAllViews();

        // Get grouped completed tasks from the database
        Map<String, List<Task>> groupedTasks = dataBaseHelper.getCompletedTasksGroupedByDate();

        if (groupedTasks.isEmpty()) {
            // Display a message if there are no completed tasks
            TextView noTasksTextView = new TextView(getContext());
            noTasksTextView.setText("No completed tasks.");
            noTasksTextView.setTextSize(16);
            noTasksTextView.setPadding(16, 16, 16, 16);
            taskContainer.addView(noTasksTextView);
            return; // Exit the method
        }

        // Dynamically add tasks to the layout
        for (String date : groupedTasks.keySet()) {
            // Add the date as a TextView
            TextView dateTextView = new TextView(getContext());
            dateTextView.setText(date);
            dateTextView.setTextSize(18);
            dateTextView.setPadding(8, 16, 8, 8);
            dateTextView.setBackgroundColor(getResources().getColor(R.color.teal_200));
            taskContainer.addView(dateTextView);

            // Add tasks for this date
            List<Task> tasks = groupedTasks.get(date);
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

                // Set click listener for edit icon
                editIcon.setOnClickListener(v -> showEditDialog(task));

                // Set click listener for delete icon
                deleteIcon.setOnClickListener(v -> showDeleteConfirmationDialog(task.getId()));

                // Set click listener for share icon
                shareIcon.setOnClickListener(v -> shareTaskViaEmail(task));

                // Add task layout to the container
                taskContainer.addView(taskLayout);
            }
        }
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

    private void showDeleteConfirmationDialog(int taskId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Delete the task from the database
            dataBaseHelper.deleteTask(taskId);
            Toast.makeText(getContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show();

            // Refresh the task list
            loadCompletedTasks();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showEditDialog(Task task) {
        // Create the custom dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_task, null);

        // Initialize dialog UI elements
        EditText taskTitle = dialogView.findViewById(R.id.edit_task_title);
        EditText taskDescription = dialogView.findViewById(R.id.edit_task_description);
        Button dueDateButton = dialogView.findViewById(R.id.edit_select_due_date);
        Spinner prioritySpinner = dialogView.findViewById(R.id.edit_priority_spinner);
        CheckBox completedCheckbox = dialogView.findViewById(R.id.edit_completed_checkbox);

        // Populate fields with existing task data
        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());
        dueDateButton.setText(task.getDueDate());
        completedCheckbox.setChecked(task.isCompleted());

        // Set date picker for due date button
        dueDateButton.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
                String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                dueDateButton.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Task");
        builder.setView(dialogView);
        builder.setPositiveButton("Update", (dialog, which) -> {
            // Update task properties
            task.setTitle(taskTitle.getText().toString().trim());
            task.setDescription(taskDescription.getText().toString().trim());
            task.setDueDate(dueDateButton.getText().toString().trim());
            task.setPriority(prioritySpinner.getSelectedItem().toString());
            task.setCompleted(completedCheckbox.isChecked());

            // Save the updated task to the database
            dataBaseHelper.updateTask(task);
            Toast.makeText(getContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();

            // Refresh the task list
            loadCompletedTasks();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
