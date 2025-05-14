package com.example.mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilalk.adapter.JobAdAdapter;
import com.example.mobilalk.model.JobAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewJobAds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobAds = new ArrayList<>();
        adapter = new JobAdAdapter(jobAds, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddJobAd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddJobAdActivity.class)));

        loadJobAds();
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
        loadJobAds();
    }
} 