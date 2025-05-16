package com.example.mobilalk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilalk.model.JobAd;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditJobAdActivity extends AppCompatActivity {
    private Button buttonSave, buttonGetLocation;
    private TextInputEditText editTextTitle, editTextDescription, editTextCategory, editTextGrossSalary, editTextLocation;
    private TextInputLayout titleInputLayout;
    private Uri selectedImageUri;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String jobAdId;
    private JobAd jobAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job_ad); // vagy activity_edit_job_ad

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        buttonSave = findViewById(R.id.buttonSave);
        buttonGetLocation = findViewById(R.id.buttonGetLocation);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextGrossSalary = findViewById(R.id.editTextGrossSalary);
        editTextLocation = findViewById(R.id.editTextLocation);
        titleInputLayout = findViewById(R.id.titleInputLayout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        jobAdId = getIntent().getStringExtra("jobAdId");
        if (jobAdId == null) {
            Toast.makeText(this, "Hirdetés nem található", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadJobAd();

        buttonSave.setOnClickListener(v -> saveJobAd());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadJobAd() {
        db.collection("jobads").document(jobAdId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    jobAd = documentSnapshot.toObject(JobAd.class);
                    if (jobAd != null) {
                        fillFields(jobAd);
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

    private void fillFields(JobAd jobAd) {
        editTextTitle.setText(jobAd.getTitle());
        editTextDescription.setText(jobAd.getDescription());
        editTextCategory.setText(jobAd.getCategory());
        editTextGrossSalary.setText(jobAd.getGrossSalary() != null ? String.valueOf(jobAd.getGrossSalary()) : "");
        editTextLocation.setText(jobAd.getLocation());
    }

    private void saveJobAd() {
        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";
        String description = editTextDescription.getText() != null ? editTextDescription.getText().toString().trim() : "";
        String category = editTextCategory.getText() != null ? editTextCategory.getText().toString().trim() : "";
        String grossSalaryStr = editTextGrossSalary.getText() != null ? editTextGrossSalary.getText().toString().trim() : "";
        String location = editTextLocation.getText() != null ? editTextLocation.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            titleInputLayout.setError("A cím megadása kötelező!");
            return;
        } else if (title.length() > 50) {
            titleInputLayout.setError("A cím legfeljebb 50 karakter lehet!");
            return;
        } else {
            titleInputLayout.setError(null);
        }

        Double grossSalary = null;
        if (!TextUtils.isEmpty(grossSalaryStr)) {
            try {
                grossSalary = Double.parseDouble(grossSalaryStr);
            } catch (NumberFormatException e) {
                editTextGrossSalary.setError("Érvénytelen összeg!");
                return;
            }
        }

        buttonSave.setEnabled(false);
        buttonSave.setText("Mentés...");

        final Double finalGrossSalary = grossSalary;
        saveJobAdToFirestore(title, description, category, finalGrossSalary, location, jobAd.getPhotoUrl());
    }

    private void saveJobAdToFirestore(String title, String description, String category, Double grossSalary, String location, String photoUrl) {
        String userId = jobAd.getUserId();
        JobAd updatedJobAd = new JobAd(title, description, category, grossSalary, location, photoUrl, null, null, userId);
        updatedJobAd.setId(jobAdId);
        db.collection("jobads").document(jobAdId)
                .set(updatedJobAd)
                .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Hirdetés sikeresen módosítva!", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("jobAdId", jobAdId);
                setResult(RESULT_OK, resultIntent);
                finish();
                })
                .addOnFailureListener(e -> {
                    buttonSave.setEnabled(true);
                    buttonSave.setText("Mentés");
                    Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}