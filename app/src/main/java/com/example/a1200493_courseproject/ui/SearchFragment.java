package com.example.a1200493_courseproject.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.a1200493_courseproject.DataBaseHelper;
import com.example.a1200493_courseproject.R;
import com.example.a1200493_courseproject.ui.Task;

import java.util.Calendar;
import java.util.List;

public class SearchFragment extends Fragment {

    private DataBaseHelper dataBaseHelper;
    private EditText startDateEditText, endDateEditText, keywordEditText;
    private Button searchByDateButton, searchByKeywordButton;
    private LinearLayout taskContainer;

    private String selectedStartDate = "";
    private String selectedEndDate = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize database helper
        dataBaseHelper = new DataBaseHelper(getContext(), "User", null, 2);

        // Initialize views
        startDateEditText = root.findViewById(R.id.edit_start_date);
        endDateEditText = root.findViewById(R.id.edit_end_date);
        keywordEditText = root.findViewById(R.id.edit_keyword);
        searchByDateButton = root.findViewById(R.id.search_by_date_button);
        searchByKeywordButton = root.findViewById(R.id.search_by_keyword_button);
        taskContainer = root.findViewById(R.id.task_container);

        // Set date pickers for start and end date
        startDateEditText.setOnClickListener(v -> showDatePicker((date) -> {
            selectedStartDate = date;
            startDateEditText.setText(date);
        }));

        endDateEditText.setOnClickListener(v -> showDatePicker((date) -> {
            selectedEndDate = date;
            endDateEditText.setText(date);
        }));

        // Set search by date button listener
        searchByDateButton.setOnClickListener(v -> searchByDateRange());

        // Set search by keyword button listener
        searchByKeywordButton.setOnClickListener(v -> searchByKeyword());

        return root;
    }

    private void showDatePicker(DatePickerCallback callback) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                    callback.onDateSelected(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void searchByDateRange() {
        if (TextUtils.isEmpty(selectedStartDate) || TextUtils.isEmpty(selectedEndDate)) {
            Toast.makeText(getContext(), "Please select both start and end dates.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch tasks for the date range
        List<Task> filteredTasks = dataBaseHelper.searchTasksByDateRange(selectedStartDate, selectedEndDate);
        populateTasks(filteredTasks);
    }

    private void searchByKeyword() {
        String keyword = keywordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(getContext(), "Please enter a keyword to search.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch tasks for the keyword
        List<Task> filteredTasks = dataBaseHelper.searchTasksByKeyword(keyword);
        populateTasks(filteredTasks);
    }

    private void populateTasks(List<Task> tasks) {
        // Clear the container first
        taskContainer.removeAllViews();

        if (tasks.isEmpty()) {
            TextView noTasksTextView = new TextView(getContext());
            noTasksTextView.setText("No tasks found for the specified criteria.");
            noTasksTextView.setTextSize(16);
            noTasksTextView.setPadding(16, 16, 16, 16);
            taskContainer.addView(noTasksTextView);
        } else {
            for (Task task : tasks) {
                // Create a layout for each task
                LinearLayout taskLayout = new LinearLayout(getContext());
                taskLayout.setOrientation(LinearLayout.VERTICAL);
                taskLayout.setPadding(16, 8, 8, 8);

                // Task details
                TextView taskTextView = new TextView(getContext());
                String taskDetails = "• " + task.getTitle() +
                        "\nDescription: " + task.getDescription() +
                        "\nPriority: " + task.getPriority() +
                        "\nCompleted: " + (task.isCompleted() ? "Yes" : "No") +
                        "\nDue Date: " + task.getDueDate();
                taskTextView.setText(taskDetails);
                taskTextView.setTextSize(16);
                taskLayout.addView(taskTextView);

                taskContainer.addView(taskLayout);
            }
        }
    }

    // Callback interface for date picker
    interface DatePickerCallback {
        void onDateSelected(String date);
    }
}
