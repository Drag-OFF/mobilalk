package com.example.mobilalk;

import android.Manifest;
import androidx.core.app.NotificationCompat;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mobilalk.model.JobAd;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class AddJobAdActivity extends AppCompatActivity {
    private Button buttonSave, buttonGetLocation;
    private TextInputEditText editTextTitle, editTextDescription, editTextCategory, editTextGrossSalary, editTextLocation;
    private TextInputLayout titleInputLayout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            editTextLocation.setText(location.getLatitude() + ", " + location.getLongitude());
        }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(@NonNull String provider) {}
        @Override public void onProviderDisabled(@NonNull String provider) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job_ad);

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

        buttonGetLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            } else {
                getLocation();
            }
        });

        buttonSave.setOnClickListener(v -> saveJobAd());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, JobAdListActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        // Próbáljuk a legutóbbi ismert helyet először
        Location lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnown == null) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (lastKnown != null) {
            editTextLocation.setText(lastKnown.getLatitude() + ", " + lastKnown.getLongitude());
            return;
        }

        // Ha nincs, kérjünk friss helyet
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        } else {
            Toast.makeText(this, "Nincs elérhető helyszolgáltató!", Toast.LENGTH_SHORT).show();
        }
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
        saveJobAdToFirestore(title, description, category, grossSalary, location);
    }

    private void saveJobAdToFirestore(String title, String description, String category, Double grossSalary, String location) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        JobAd jobAd = new JobAd(title, description, category, grossSalary, location, null, null, null, userId);
        db.collection("jobads")
                .add(jobAd)
                .addOnSuccessListener(documentReference -> {
                    showSuccessNotification();
                    Toast.makeText(this, "Sikeres mentés!", Toast.LENGTH_SHORT).show();

                    setAlarmForReminder();

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setAlarmForReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        long triggerAtMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    private void showSuccessNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "jobad_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Álláshirdetés", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Sikeres feladás")
                .setContentText("Az álláshirdetésed mentve lett!")
                .setAutoCancel(true);
        manager.notify(1, builder.build());
    }
}