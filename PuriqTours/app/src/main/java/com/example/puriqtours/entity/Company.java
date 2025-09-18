package com.example.puriqtours.entity;

public class Company {
    private String name;
    private int imageResource;
    private float rating;

    public Company(String name, int imageResource, float rating) {
        this.name = name;
        this.imageResource = imageResource;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public float getRating() {
        return rating;
    }
}