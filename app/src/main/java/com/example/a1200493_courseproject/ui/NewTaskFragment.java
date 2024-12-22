package com.example.a1200493_courseproject.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.a1200493_courseproject.DataBaseHelper;
import com.example.a1200493_courseproject.R;
import com.example.a1200493_courseproject.TaskAlertReceiver;
import com.example.a1200493_courseproject.ui.Task;

import java.util.Calendar;

public class NewTaskFragment extends Fragment {

    private EditText taskTitle, taskDescription;
    private Button selectDueDate, saveTask;
    private Spinner prioritySpinner;
    private CheckBox completedCheckbox;
    private ImageView reminderIcon;
    private Calendar notificationTime;

    private String selectedPriority = "Medium";
    private String selectedDate = "";

    private DataBaseHelper dataBaseHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_task, container, false);

        // Initialize views
        taskTitle = root.findViewById(R.id.task_title);
        taskDescription = root.findViewById(R.id.task_description);
        selectDueDate = root.findViewById(R.id.select_due_date);
        saveTask = root.findViewById(R.id.save_task);
        prioritySpinner = root.findViewById(R.id.priority_spinner);
        completedCheckbox = root.findViewById(R.id.completed_checkbox);

        // Initialize reminder icon
        reminderIcon = root.findViewById(R.id.remindericon);
        notificationTime = Calendar.getInstance(); // Initialize notification time

        reminderIcon.setOnClickListener(v -> showDateTimePicker());


//        if (getArguments() != null) {
//            userEmail = getArguments().getString("userEmail");
//            Log.d("NEW_TASK", "Received user email: " + userEmail);
//        } else {
//            Log.d("NEW_TASK", "No arguments received!");
//        }


        // Initialize database helper
        dataBaseHelper = new DataBaseHelper(getContext(), "User", null, 2);

        // Set priority spinner listener
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPriority = "Medium"; // Default priority
            }
        });

        // Set due date picker
        selectDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Save task to the database
        saveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTaskToDatabase();
            }
        });

        return root;
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                selectDueDate.setText(selectedDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveTaskToDatabase() {
        String title = taskTitle.getText().toString().trim();
        String description = taskDescription.getText().toString().trim();
        boolean isCompleted = completedCheckbox.isChecked();

        if (title.isEmpty()) {
            taskTitle.setError("Task title is required!");
            return;
        }

        if (description.isEmpty()) {
            taskDescription.setError("Task description is required!");
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(getContext(), "Please select a due date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the logged-in user's email from arguments
        String userEmail = getArguments().getString("userEmail", ""); // Default to empty if null
        if (userEmail.isEmpty()) {
            // Toast.makeText(getContext(), "User email is missing. Cannot save task.", Toast.LENGTH_SHORT).show();
            //return;
            userEmail = "tjebrini8@gmail.com";
        }

        // Create a new task object
        Task task = new Task(title, description, selectedDate, selectedPriority, isCompleted, userEmail);

        // Save task to the database
        dataBaseHelper.insertTask(task);

        // Show success message
        Toast.makeText(getContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();

        // Reset fields
        taskTitle.setText("");
        taskDescription.setText("");
        selectDueDate.setText("Select Due Date");
        completedCheckbox.setChecked(false);
        prioritySpinner.setSelection(1); // Default to Medium priority
    }

    // Show Date and Time Picker Dialogs
    private void showDateTimePicker() {
        // Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            notificationTime.set(Calendar.YEAR, year);
            notificationTime.set(Calendar.MONTH, month);
            notificationTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Time Picker
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                notificationTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                notificationTime.set(Calendar.MINUTE, minute);
                notificationTime.set(Calendar.SECOND, 0);

                // Schedule the notification
                scheduleNotification("Task Reminder", "Don't forget your task!");
            }, notificationTime.get(Calendar.HOUR_OF_DAY), notificationTime.get(Calendar.MINUTE), true);

            timePickerDialog.show();
        }, notificationTime.get(Calendar.YEAR), notificationTime.get(Calendar.MONTH), notificationTime.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void scheduleNotification(String title, String body) {
        Intent intent = new Intent(getContext(), TaskAlertReceiver.class);
        intent.putExtra("taskTitle", title);
        intent.putExtra("taskDescription", body);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), pendingIntent);
            Toast.makeText(getContext(), "Reminder set for: " + notificationTime.getTime(), Toast.LENGTH_SHORT).show();
        }
    }


}
