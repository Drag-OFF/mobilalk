package com.example.mobilalk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobilalk.model.JobAd;
import com.example.mobilalk.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class JobAdDetailActivity extends AppCompatActivity {
    private ImageView imageViewPhoto;
    private TextView textViewTitle, textViewCategory, textViewGrossSalary, textViewLocation, textViewDescription;
    private Button buttonShare, buttonEdit, buttonDelete;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String jobAdId;
    private JobAd jobAd;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_ad_detail);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewCategory = findViewById(R.id.textViewCategory);
        textViewGrossSalary = findViewById(R.id.textViewGrossSalary);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewDescription = findViewById(R.id.textViewDescription);
        buttonShare = findViewById(R.id.buttonShare);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        jobAdId = getIntent().getStringExtra("jobAdId");
        if (jobAdId == null) {
            Toast.makeText(this, "Hirdetés nem található", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadJobAd();
        checkAdminStatus();
    }

    private void loadJobAd() {
        db.collection("jobads").document(jobAdId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    jobAd = documentSnapshot.toObject(JobAd.class);
                    if (jobAd != null) {
                        displayJobAd();
                    } else {
                        Toast.makeText(this, "Hirdetés nem található", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayJobAd() {
        textViewTitle.setText(jobAd.getTitle());
        textViewCategory.setText(jobAd.getCategory() != null ? jobAd.getCategory() : "");
        textViewGrossSalary.setText(jobAd.getGrossSalary() != null ? String.format("%,.0f Ft", jobAd.getGrossSalary()) : "");
        textViewLocation.setText(jobAd.getLocation() != null ? jobAd.getLocation() : "");
        textViewDescription.setText(jobAd.getDescription() != null ? jobAd.getDescription() : "");
        if (jobAd.getPhotoUrl() != null && !jobAd.getPhotoUrl().isEmpty()) {
            Glide.with(this).load(jobAd.getPhotoUrl()).centerCrop().into(imageViewPhoto);
        } else {
            imageViewPhoto.setImageResource(android.R.drawable.ic_menu_info_details);
        }
        buttonShare.setOnClickListener(v -> shareJobAd());
    }

    private void shareJobAd() {
        StringBuilder sb = new StringBuilder();
        sb.append("Állás: ").append(jobAd.getTitle()).append("\n");
        if (jobAd.getCategory() != null && !jobAd.getCategory().isEmpty())
            sb.append("Kategória: ").append(jobAd.getCategory()).append("\n");
        if (jobAd.getGrossSalary() != null)
            sb.append("Bruttó bér: ").append(String.format("%,.0f Ft", jobAd.getGrossSalary())).append("\n");
        if (jobAd.getLocation() != null && !jobAd.getLocation().isEmpty())
            sb.append("Helyszín: ").append(jobAd.getLocation()).append("\n");
        if (jobAd.getDescription() != null && !jobAd.getDescription().isEmpty())
            sb.append("Leírás: ").append(jobAd.getDescription()).append("\n");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Álláshirdetés megosztása"));
    }

    private void checkAdminStatus() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.isAdmin()) {
                        isAdmin = true;
                        buttonEdit.setVisibility(View.VISIBLE);
                        buttonDelete.setVisibility(View.VISIBLE);
                        buttonEdit.setOnClickListener(v -> editJobAd());
                        buttonDelete.setOnClickListener(v -> confirmDelete());
                    }
                });
    }

    private void editJobAd() {
        Intent intent = new Intent(this, EditJobAdActivity.class);
        intent.putExtra("jobAdId", jobAdId);
        startActivity(intent);
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hirdetés törlése")
                .setMessage("Biztosan törölni szeretnéd ezt a hirdetést?")
                .setPositiveButton("Törlés", (dialog, which) -> deleteJobAd())
                .setNegativeButton("Mégse", null)
                .show();
    }

    private void deleteJobAd() {
        db.collection("jobads").document(jobAdId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Hirdetés törölve!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
} 