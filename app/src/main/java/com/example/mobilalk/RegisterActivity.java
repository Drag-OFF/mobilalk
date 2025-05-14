package com.example.mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.mobilalk.model.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ImageButton backButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        backButton = findViewById(R.id.backButton);

        // Apply fade in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.registerTitleTextView).startAnimation(fadeIn);
        findViewById(R.id.emailInputLayout).startAnimation(fadeIn);
        findViewById(R.id.passwordInputLayout).startAnimation(fadeIn);
        findViewById(R.id.confirmPasswordInputLayout).startAnimation(fadeIn);
        registerButton.startAnimation(fadeIn);
        loginTextView.startAnimation(fadeIn);

        registerButton.setOnClickListener(v -> registerUser());
        
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
        
        backButton.setOnClickListener(v -> {
            // Go back to the previous activity or close if there's no previous
            if (!isTaskRoot()) {
                onBackPressed();
            } else {
                // If this is the root activity, go to the main screen
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        // Check if this is an admin registration
        boolean isAdmin = email.equals("admin@example.com");

        // Show loading indicator
        registerButton.setEnabled(false);

        // Create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    registerButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Create user document in Firestore
                        String uid = auth.getCurrentUser().getUid();
                        User user = new User(uid, email, isAdmin);
                        
                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this,
                                            "Registration successful",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this,
                                            JobAdListActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this,
                                            "Error creating user profile: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 