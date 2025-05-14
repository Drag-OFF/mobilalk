package com.example.mobilalk.model;

public class User {
    private String uid;
    private String email;
    private boolean isAdmin;

    // Empty constructor needed for Firestore
    public User() {}

    public User(String uid, String email, boolean isAdmin) {
        this.uid = uid;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
} 