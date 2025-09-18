package com.example.puriqtours.entity;

public class Tour {
    private String title;
    private String location;
    private String status;
    private int rating;

    public Tour(String title, String location, String status, int rating) {
        this.title = title;
        this.location = location;
        this.status = status;
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public int getRating() {
        return rating;
    }
}