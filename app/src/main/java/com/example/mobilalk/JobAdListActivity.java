package com.example.mobilalk;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilalk.adapter.JobAdAdapter;
import com.example.mobilalk.model.JobAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class JobAdListActivity extends AppCompatActivity implements JobAdAdapter.OnJobAdClickListener {
    private RecyclerView recyclerView;
    private JobAdAdapter adapter;
    private List<JobAd> jobAds;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_ad_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPopupTheme(R.style.AppTheme_PopupMenu);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobAds = new ArrayList<>();
        adapter = new JobAdAdapter(jobAds, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddJobAd = findViewById(R.id.fabAddJobAd);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && "admin@example.com".equals(user.getEmail())) {
            fabAddJobAd.setVisibility(View.VISIBLE);
        } else {
            fabAddJobAd.setVisibility(View.GONE);
        }

        fabAddJobAd.setOnClickListener(v -> startActivity(new Intent(this, AddJobAdActivity.class)));

        loadJobAds();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        menu.add(0, 99, 0, "Összes");
        menu.add(0, 100, 1, "Kategória: IT");
        menu.add(0, 101, 2, "Top 10 fizetés");
        menu.add(0, 102, 3, "Cím: fejlesztő");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == 99) {
            // Összes hirdetés
            loadJobAds();
            return true;
        } else if (item.getItemId() == 100) {
            // 1. Kategória szerinti szűrés
            db.collection("jobads").whereEqualTo("category", "IT").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobAds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        JobAd jobAd = doc.toObject(JobAd.class);
                        jobAd.setId(doc.getId());
                        jobAds.add(jobAd);
                    }
                    adapter.updateJobAds(jobAds);
                });
            return true;
        } else if (item.getItemId() == 101) {
            // 2. Fizetés szerinti rendezés, limitálás
            db.collection("jobads").orderBy("grossSalary", Query.Direction.DESCENDING).limit(10).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobAds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        JobAd jobAd = doc.toObject(JobAd.class);
                        jobAd.setId(doc.getId());
                        jobAds.add(jobAd);
                    }
                    adapter.updateJobAds(jobAds);
                });
            return true;
        } else if (item.getItemId() == 102) {
            // 3. Keresés cím alapján
            db.collection("jobads")
                .whereGreaterThanOrEqualTo("title", "fejlesztő")
                .whereLessThanOrEqualTo("title", "fejlesztő\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobAds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        JobAd jobAd = doc.toObject(JobAd.class);
                        jobAd.setId(doc.getId());
                        jobAds.add(jobAd);
                    }
                    adapter.updateJobAds(jobAds);
                });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadJobAds() {
        db.collection("jobads")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobAds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        JobAd jobAd = doc.toObject(JobAd.class);
                        jobAd.setId(doc.getId());
                        jobAds.add(jobAd);
                    }
                    adapter.updateJobAds(jobAds);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hiba a hirdetések betöltésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onJobAdClick(JobAd jobAd) {
        Intent intent = new Intent(this, JobAdDetailActivity.class);
        intent.putExtra("jobAdId", jobAd.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobAds(); // újratölti a hirdetéseket Firestore-ból
    }
}