package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText txt_email, txt_password;
    private Button login;
    private TextView goToSignup, forgotPassword;
    private CheckBox rememberMe;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Ensure your XML is named correctly

        // Initialize UI components
        txt_email = findViewById(R.id.email);
        txt_password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        goToSignup = findViewById(R.id.goToSignup);
        forgotPassword = findViewById(R.id.forgotPassword);
        rememberMe = findViewById(R.id.rememberMe);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(v -> {
            String email = txt_email.getText().toString().trim();
            String password = txt_password.getText().toString().trim();

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Debug log
            Log.d("LoginDebug", "Email: " + email + ", Password: " + password);

            // Attempt login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            // Handle common auth errors
                            Exception e = task.getException();
                            Log.e("LoginError", "Login failed", e);

                            String message = "Login failed. Please try again.";
                            if (e != null && e.getMessage() != null) {
                                String msg = e.getMessage();
                                if (msg.contains("There is no user record")) {
                                    message = "No account found with that email.";
                                } else if (msg.contains("The password is invalid")) {
                                    message = "Incorrect password.";
                                } else if (msg.contains("badly formatted")) {
                                    message = "Invalid email format.";
                                }
                            }

                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Navigate to signup
        goToSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        // Placeholder for password reset
        forgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
            // You can add ForgotPasswordActivity here later
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Auto-login if already authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
