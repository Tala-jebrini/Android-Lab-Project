package com.example.a1200493_courseproject.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.a1200493_courseproject.DataBaseHelper;
import com.example.a1200493_courseproject.R;
import com.example.a1200493_courseproject.SharedPrefManager;

public class ProfileFragment extends Fragment {

    private DataBaseHelper dataBaseHelper;
    private EditText emailEditText, oldPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button updateButton;
    private CheckBox checkbox;

    private String currentEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize database helper
        dataBaseHelper = new DataBaseHelper(getContext(), "User", null, 2);

        // Retrieve current email from shared preferences
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        currentEmail = sharedPrefManager.readString("email", "");

        // Initialize views
        emailEditText = root.findViewById(R.id.edit_email);
        oldPasswordEditText = root.findViewById(R.id.edit_old_password);
        newPasswordEditText = root.findViewById(R.id.edit_new_password);
        confirmPasswordEditText = root.findViewById(R.id.edit_confirm_password);
        updateButton = root.findViewById(R.id.update_button);
        checkbox = root.findViewById(R.id.checkBox);

        // Prefill email
        emailEditText.setText(currentEmail);

        // Set update button listener
        updateButton.setOnClickListener(v -> updateProfile());

        boolean savedMode = sharedPrefManager.getBoolean("Mode", false);
        applyDarkMode(savedMode);
        checkbox.setChecked(savedMode);

        // Listen for CheckBox clicks
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isChecked = checkbox.isChecked();

                // Apply the appropriate mode
                applyDarkMode(isChecked);

                // Save the preference
                sharedPrefManager.setBoolean("Mode", isChecked);
            }
        });

        return root;
    }

    private void updateProfile() {
        String newEmail = emailEditText.getText().toString().trim();
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs
        if (newEmail.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dataBaseHelper.isPasswordCorrect(currentEmail, oldPassword)) {
            Toast.makeText(getContext(), "Old password is incorrect.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New password and confirmation do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "New password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Update Profile")
                .setMessage("Are you sure you want to update your profile?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Update user in the database
                    boolean isUpdated = dataBaseHelper.updateUser(currentEmail, newEmail, newPassword);

                    if (isUpdated) {
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                        // Update shared preferences with the new email
                        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
                        sharedPrefManager.writeString("email", newEmail);

                        currentEmail = newEmail;

                        // Clear password fields after successful update
                        oldPasswordEditText.setText("");
                        newPasswordEditText.setText("");
                        confirmPasswordEditText.setText("");
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void applyDarkMode(boolean enable){
        if (enable){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
