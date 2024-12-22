package com.example.a1200493_courseproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Signup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText Email = findViewById(R.id.email);
        EditText FirstName = findViewById(R.id.firstname);
        EditText LastName = findViewById(R.id.lastname);
        EditText Password = findViewById(R.id.password);
        EditText ConfirmPassword = findViewById(R.id.confirmPassword);

        TextView emailText = findViewById(R.id.emailText);
        TextView firstnameText = findViewById(R.id.firstnameText);
        TextView lastnameText = findViewById(R.id.lastnameText);
        TextView passwordText = findViewById(R.id.passwordText);
        TextView confirmpasswordText = findViewById(R.id.confirmpasswordText);

        Button SignupButton = findViewById(R.id.SignupButton);
        Button BackButton = findViewById(R.id.BackButton);

        DataBaseHelper dataBaseHelper = new DataBaseHelper(Signup.this, "User", null, 2);


        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve user input
                String emailInput = Email.getText().toString().trim();
                String firstNameInput = FirstName.getText().toString().trim();
                String lastNameInput = LastName.getText().toString().trim();
                String passwordInput = Password.getText().toString();
                String confirmPasswordInput = ConfirmPassword.getText().toString();

                // Reset all error messages
                emailText.setText("");
                firstnameText.setText("");
                lastnameText.setText("");
                passwordText.setText("");
                confirmpasswordText.setText("");

                // Reset field colors to default
                Email.setBackgroundResource(R.drawable.rounded_edittext);
                FirstName.setBackgroundResource(R.drawable.rounded_edittext);
                LastName.setBackgroundResource(R.drawable.rounded_edittext);
                Password.setBackgroundResource(R.drawable.rounded_edittext);
                ConfirmPassword.setBackgroundResource(R.drawable.rounded_edittext);

                // Reset passwordText height
                passwordText.getLayoutParams().height = (int) (23 * getResources().getDisplayMetrics().density);
                passwordText.requestLayout();


                // Validation flags
                boolean isValid = true;

                // Validate email
                if (TextUtils.isEmpty(emailInput)) {
                    emailText.setText("Email is required.");
                    Email.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    emailText.setText("Invalid email format.");
                    Email.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }
                else if (dataBaseHelper.userExists(emailInput)) {
                    emailText.setText("This email already has an account!");
                    Email.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }

                // Validate first name
                if (TextUtils.isEmpty(firstNameInput)) {
                    firstnameText.setText("First name is required.");
                    FirstName.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                } else if (firstNameInput.length() < 5 || firstNameInput.length() > 20) {
                    firstnameText.setText("First name must be 5-20 characters.");
                    FirstName.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }

                // Validate last name
                if (TextUtils.isEmpty(lastNameInput)) {
                    lastnameText.setText("Last name is required.");
                    LastName.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                } else if (lastNameInput.length() < 5 || lastNameInput.length() > 20) {
                    lastnameText.setText("Last name must be 5-20 characters.");
                    LastName.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }

                // Validate password
                if (TextUtils.isEmpty(passwordInput)) {
                    passwordText.setText("Password is required.");
                    Password.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                } else if (!isValidPassword(passwordInput)) {
                    passwordText.setText("Password must be 6-12 characters, include one number, one lowercase, and one uppercase letter.");
                    // Dynamically change the height
                    passwordText.getLayoutParams().height = (int) (45 * getResources().getDisplayMetrics().density);
                    passwordText.requestLayout(); // Apply the new height

                    Password.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }

                // Validate confirm password
                if (TextUtils.isEmpty(confirmPasswordInput)) {
                    confirmpasswordText.setText("Confirm password is required.");
                    ConfirmPassword.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                } else if (!passwordInput.equals(confirmPasswordInput)) {
                    confirmpasswordText.setText("Passwords do not match.");
                    ConfirmPassword.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }

                // If all fields are valid, proceed to the next activity
                if (isValid) {

                    User newUser = new User(emailInput, firstNameInput, lastNameInput, passwordInput);
                    // Insert the user into the database
                    try {
                        dataBaseHelper.insertUser(newUser);
                    } catch (Exception e) {
                        emailText.setText("An error occurred. Please try again.");
                    }

                    Intent intent = new Intent(Signup.this, Home.class);
                    startActivity(intent);
                    finish();

                }
            }
        });

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Method to validate password criteria
    private boolean isValidPassword(String password) {
        if (password.length() < 6 || password.length() > 12) return false;
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            if (Character.isLowerCase(c)) hasLowercase = true;
            if (Character.isDigit(c)) hasNumber = true;
        }
        return hasUppercase && hasLowercase && hasNumber;
    }
}
