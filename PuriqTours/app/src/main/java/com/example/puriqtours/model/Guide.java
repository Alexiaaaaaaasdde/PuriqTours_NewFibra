package com.example.puriqtours.model;

public class Guide {
    private int id;
    private String name;
    private String location;
    private int rating;
    private boolean isAvailable;
    private int imageResource;

    public Guide() {}

    public Guide(int id, String name, String location, int rating, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.isAvailable = isAvailable;
    }

    public Guide(int id, String name, String location, int rating, boolean isAvailable, int imageResource) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.isAvailable = isAvailable;
        this.imageResource = imageResource;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
}