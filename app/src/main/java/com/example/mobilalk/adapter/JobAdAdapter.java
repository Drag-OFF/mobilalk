package com.example.mobilalk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilalk.R;
import com.example.mobilalk.model.JobAd;

import java.util.List;

public class JobAdAdapter extends RecyclerView.Adapter<JobAdAdapter.JobAdViewHolder> {
    private List<JobAd> jobAds;
    private OnJobAdClickListener listener;

    public interface OnJobAdClickListener {
        void onJobAdClick(JobAd jobAd);
    }

    public JobAdAdapter(List<JobAd> jobAds, OnJobAdClickListener listener) {
        this.jobAds = jobAds;
        this.listener = listener;
    }

    public void updateJobAds(List<JobAd> newJobAds) {
        this.jobAds = newJobAds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobAdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_ad, parent, false);
        return new JobAdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobAdViewHolder holder, int position) {
        holder.bind(jobAds.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return jobAds.size();
    }

    static class JobAdViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewPhoto;
        private final TextView textViewTitle, textViewCategory, textViewLocation, textViewGrossSalary;

        public JobAdViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewGrossSalary = itemView.findViewById(R.id.textViewGrossSalary);
        }

        public void bind(final JobAd jobAd, final OnJobAdClickListener listener) {
            textViewTitle.setText(jobAd.getTitle());
            textViewCategory.setText(jobAd.getCategory() != null ? jobAd.getCategory() : "");
            textViewLocation.setText(jobAd.getLocation() != null ? jobAd.getLocation() : "");
            if (jobAd.getGrossSalary() != null) {
                textViewGrossSalary.setText(String.format("%,.0f Ft", jobAd.getGrossSalary()));
            } else {
                textViewGrossSalary.setText("");
            }
            if (jobAd.getPhotoUrl() != null && !jobAd.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(jobAd.getPhotoUrl())
                        .centerCrop()
                        .into(imageViewPhoto);
            } else {
                imageViewPhoto.setImageResource(android.R.drawable.ic_menu_info_details);
            }
            itemView.setOnClickListener(v -> listener.onJobAdClick(jobAd));
        }
    }
} 