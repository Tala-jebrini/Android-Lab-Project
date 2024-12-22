package com.example.a1200493_courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignIn extends AppCompatActivity {

    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        EditText Email = findViewById(R.id.email);
        EditText Password = findViewById(R.id.password);

        TextView emailText = findViewById(R.id.emailText);
        TextView passwordText = findViewById(R.id.passwordText);

        CheckBox checkbox = findViewById(R.id.checkBox);

        Button SignInButton = findViewById(R.id.SignInButton);
        Button BackButton = findViewById(R.id.BackButton);

        DataBaseHelper dataBaseHelper = new DataBaseHelper(SignIn.this, "User", null, 2);

        // Initialize SharedPrefManager
        sharedPrefManager = SharedPrefManager.getInstance(SignIn.this);

        String savedEmail = sharedPrefManager.readString("email", "");
        if (!TextUtils.isEmpty(savedEmail)) {
            Email.setText(savedEmail);
        }

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailInput = Email.getText().toString().trim();
                String passwordInput = Password.getText().toString();

                // Reset all error messages
                emailText.setText("");
                passwordText.setText("");

                // Reset field colors to default
                Email.setBackgroundResource(R.drawable.rounded_edittext);
                Password.setBackgroundResource(R.drawable.rounded_edittext);

                boolean isValid = true;

                // Check if email field is empty
                if (TextUtils.isEmpty(emailInput)) {
                    emailText.setText("Enter your email please.");
                    Email.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }

                // Check if password field is empty
                if (TextUtils.isEmpty(passwordInput)) {
                    passwordText.setText("Enter your password please.");
                    Password.setBackgroundResource(R.drawable.edittext_error);
                    isValid = false;
                }

                // If both fields are filled, check if email exists in the database
                if (isValid) {
                    if (!dataBaseHelper.userExists(emailInput)) {
                        emailText.setText("This email is not registered.");
                        Email.setBackgroundResource(R.drawable.edittext_error);
                        isValid = false;
                    } else if (!dataBaseHelper.isPasswordCorrect(emailInput, passwordInput)) {
                        passwordText.setText("Incorrect password.");
                        Password.setBackgroundResource(R.drawable.edittext_error);
                        isValid = false;
                    }
                }

                // Proceed if email and password are correct
                if (isValid) {
                    if (checkbox.isChecked()) {
                        sharedPrefManager.writeString("email", emailInput);
                        Toast.makeText(SignIn.this, "Email saved", Toast.LENGTH_SHORT).show();
                    }

                    Intent intent = new Intent(SignIn.this, Home.class);
                    intent.putExtra("userEmail", emailInput); // Pass the user's email
                    startActivity(intent);
                    finish();

                }
            }
        });

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
