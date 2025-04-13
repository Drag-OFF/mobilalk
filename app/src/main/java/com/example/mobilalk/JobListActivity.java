package com.example.mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class JobListActivity extends AppCompatActivity implements JobAdapter.OnJobClickListener {
    private RecyclerView recyclerView;
    private JobAdapter adapter;
    private TextView jobListTitleTextView;
    private LinearLayout logoutLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_list);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        initializeUI();
        setupJobList();
        setupClickListeners();
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        jobListTitleTextView = findViewById(R.id.jobListTitleTextView);
        logoutLayout = findViewById(R.id.logoutLayout);

        // Apply fade in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        jobListTitleTextView.startAnimation(fadeIn);
        recyclerView.startAnimation(fadeIn);
        logoutLayout.startAnimation(fadeIn);
    }

    private void setupJobList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter(getSampleJobs(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        logoutLayout.setOnClickListener(v -> {
            // Show confirmation dialog
            new android.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(JobListActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(JobListActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
        });
    }

    private List<Job> getSampleJobs() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("Software Developer", "Tech Corp", "New York", "Full-time", "$100,000", "Looking for experienced software developers..."));
        jobs.add(new Job("UX Designer", "Design Studio", "San Francisco", "Contract", "$90,000", "Join our creative team..."));
        jobs.add(new Job("Project Manager", "Business Solutions", "Chicago", "Full-time", "$120,000", "Lead our development projects..."));
        return jobs;
    }

    @Override
    public void onJobClick(Job job) {
        Intent intent = new Intent(this, JobDetailActivity.class);
        intent.putExtra("job", job);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No user is signed in, redirect to login
            startActivity(new Intent(JobListActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }
} 