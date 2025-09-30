package com.example.puriqtours.model;

public class Tour {
    private int id;
    private String name;
    private String description;
    private String location;
    private double price;
    private int duration;
    private String guideAssigned;
    private int imageResource;
    private String date;
    private boolean isAvailable;

    public Tour() {}

    public Tour(int id, String name, String description, String location, double price, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.price = price;
        this.duration = duration;
        this.isAvailable = true;
    }

    public Tour(int id, String name, String location, String description, String date, int imageResource, double price, int duration, String guideAssigned) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.date = date;
        this.imageResource = imageResource;
        this.price = price;
        this.duration = duration;
        this.guideAssigned = guideAssigned;
        this.isAvailable = true;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getGuideAssigned() { return guideAssigned; }
    public void setGuideAssigned(String guideAssigned) { this.guideAssigned = guideAssigned; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    // Método para obtener el estado como string
    public String getStatus() {
        if (guideAssigned != null && !guideAssigned.isEmpty()) {
            return "Guía asignado: " + guideAssigned;
        } else {
            return "Sin guía asignado";
        }
    }

    // Método para obtener duración como string formateada
    public String getDurationText() {
        return date != null ? date : duration + " días";
    }
}