package com.example.mobilalk;

import java.io.Serializable;

public class Job implements Serializable {
    private String title;
    private String company;
    private String description;
    private String location;
    private String type;
    private String salary;

    public Job(String title, String company, String description, String location, String type, String salary) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.location = location;
        this.type = type;
        this.salary = salary;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public String getSalary() {
        return salary;
    }
} 