package com.example.mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class JobDetailActivity extends AppCompatActivity {
    private TextView jobTitleTextView;
    private TextView companyTextView;
    private TextView locationTextView;
    private TextView typeTextView;
    private TextView salaryTextView;
    private TextView descriptionTextView;
    private Button applyButton;
    private ImageButton backButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check authentication first
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No user is signed in, redirect to login
            startActivity(new Intent(JobDetailActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
            return;
        }

        initializeViews();
        setupJobDetails();
        setupClickListeners();
    }

    private void initializeViews() {
        jobTitleTextView = findViewById(R.id.jobTitleTextView);
        companyTextView = findViewById(R.id.companyTextView);
        locationTextView = findViewById(R.id.locationTextView);
        typeTextView = findViewById(R.id.typeTextView);
        salaryTextView = findViewById(R.id.salaryTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        applyButton = findViewById(R.id.applyButton);
        backButton = findViewById(R.id.backButton);

        // Apply fade in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        jobTitleTextView.startAnimation(fadeIn);
        companyTextView.startAnimation(fadeIn);
        locationTextView.startAnimation(fadeIn);
        typeTextView.startAnimation(fadeIn);
        salaryTextView.startAnimation(fadeIn);
        descriptionTextView.startAnimation(fadeIn);
        applyButton.startAnimation(fadeIn);
        backButton.startAnimation(fadeIn);
    }

    private void setupJobDetails() {
        Job job = getIntent().getParcelableExtra("job");
        if (job != null) {
            jobTitleTextView.setText(job.getTitle());
            companyTextView.setText(job.getCompany());
            locationTextView.setText(job.getLocation());
            typeTextView.setText(job.getType());
            salaryTextView.setText(job.getSalary());
            descriptionTextView.setText(job.getDescription());
        }
    }

    private void setupClickListeners() {
        applyButton.setOnClickListener(v -> {
            // Show loading state
            applyButton.setEnabled(false);
            
            // Simulate application process
            new Handler().postDelayed(() -> {
                applyButton.setEnabled(true);
                Toast.makeText(JobDetailActivity.this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
            }, 1500);
        });

        backButton.setOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No user is signed in, redirect to login
            startActivity(new Intent(JobDetailActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }
} 