package com.example.mobilalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.mobilalk.model.JobAd;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddJobAdActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;

    private ImageView imageViewPhoto;
    private Button buttonSelectPhoto, buttonCamera, buttonFillLocation, buttonSave;
    private TextInputEditText editTextTitle, editTextDescription, editTextCategory, editTextGrossSalary, editTextLocation;
    private TextInputLayout titleInputLayout;
    private Uri selectedImageUri, cameraImageUri;
    private Double latitude = null, longitude = null;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job_ad);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonSelectPhoto = findViewById(R.id.buttonSelectPhoto);
        buttonCamera = findViewById(R.id.buttonCamera);
        buttonFillLocation = findViewById(R.id.buttonFillLocation);
        buttonSave = findViewById(R.id.buttonSave);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextGrossSalary = findViewById(R.id.editTextGrossSalary);
        editTextLocation = findViewById(R.id.editTextLocation);
        titleInputLayout = findViewById(R.id.titleInputLayout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        buttonSelectPhoto.setOnClickListener(v -> openGallery());
        buttonCamera.setOnClickListener(v -> openCamera());
        buttonFillLocation.setOnClickListener(v -> requestLocation());
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imageViewPhoto.setImageURI(selectedImageUri);
                }
            });

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            } catch (IOException ex) {
                Toast.makeText(this, "Nem sikerült képet készíteni", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImageUri = cameraImageUri;
            imageViewPhoto.setImageURI(selectedImageUri);
        }
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // Próbáljuk szöveges helyszínné alakítani
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String city = addresses.get(0).getLocality();
                        String address = addresses.get(0).getAddressLine(0);
                        editTextLocation.setText(city != null ? city : address);
                    } else {
                        editTextLocation.setText(latitude + ", " + longitude);
                    }
                } catch (IOException e) {
                    editTextLocation.setText(latitude + ", " + longitude);
                }
            } else {
                Toast.makeText(this, "Nem sikerült lekérni a helyzetet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveJobAd() {
        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";
        String description = editTextDescription.getText() != null ? editTextDescription.getText().toString().trim() : "";
        String category = editTextCategory.getText() != null ? editTextCategory.getText().toString().trim() : "";
        String grossSalaryStr = editTextGrossSalary.getText() != null ? editTextGrossSalary.getText().toString().trim() : "";
        String location = editTextLocation.getText() != null ? editTextLocation.getText().toString().trim() : "";

        // Validáció
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
        if (selectedImageUri != null) {
            // Kép feltöltése
            String imageId = UUID.randomUUID().toString();
            StorageReference imageRef = storage.getReference().child("jobad_images/" + imageId);
            imageRef.putFile(selectedImageUri)
                    .continueWithTask(task -> imageRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> saveJobAdToFirestore(title, description, category, finalGrossSalary, location, uri.toString()))
                    .addOnFailureListener(e -> {
                        buttonSave.setEnabled(true);
                        buttonSave.setText("Álláshirdetés feladása");
                        Toast.makeText(this, "Kép feltöltése sikertelen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveJobAdToFirestore(title, description, category, grossSalary, location, null);
        }
    }

    private void saveJobAdToFirestore(String title, String description, String category, Double grossSalary, String location, String photoUrl) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        JobAd jobAd = new JobAd(title, description, category, grossSalary, location, photoUrl, latitude, longitude, userId);
        db.collection("jobads")
                .add(jobAd)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Álláshirdetés sikeresen feladva!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    buttonSave.setEnabled(true);
                    buttonSave.setText("Álláshirdetés feladása");
                    Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
} 