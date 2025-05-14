package com.example.mobilalk.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class JobAd {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String category;
    private Double grossSalary;
    private String location;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String userId;
    @ServerTimestamp
    private Date createdAt;

    public JobAd() {}

    public JobAd(String title, String description, String category, Double grossSalary, String location, String photoUrl, Double latitude, Double longitude, String userId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.grossSalary = grossSalary;
        this.location = location;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getGrossSalary() { return grossSalary; }
    public void setGrossSalary(Double grossSalary) { this.grossSalary = grossSalary; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
} 