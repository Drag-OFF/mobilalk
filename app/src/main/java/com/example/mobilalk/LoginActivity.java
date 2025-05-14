package com.example.mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.mobilalk.model.User;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ImageButton backButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        backButton = findViewById(R.id.backButton);

        // Apply fade in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        emailInputLayout.startAnimation(fadeIn);
        passwordInputLayout.startAnimation(fadeIn);
        loginButton.startAnimation(fadeIn);
        registerTextView.startAnimation(fadeIn);
        backButton.startAnimation(fadeIn);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> loginUser());

        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        backButton.setOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            return;
        }

        // Show loading indicator
        loginButton.setEnabled(false);

        // Sign in user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Check if user is admin
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null && user.isAdmin()) {
                                        Toast.makeText(LoginActivity.this,
                                                "Welcome Admin!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "Login successful",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    startActivity(new Intent(LoginActivity.this, JobAdListActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this,
                                            "Error checking user role: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, redirect to job list
            startActivity(new Intent(LoginActivity.this, JobListActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }
} 