package com.example.puriqtours.entity;

public class Company {

    private String id;
    private String name;
    private String ruc;
    private String email;
    private String phone;
    private String address;
    private String imageUrl;
    private double rating;
    private String status;

    public Company() {}

    public Company(String name, String ruc, String email, String phone,
                   String address, String imageUrl, double rating, String status) {
        this.name = name;
        this.ruc = ruc;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.status = status;
    }

    // 🔹 Getters y setters necesarios para Firestore + Adapter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
