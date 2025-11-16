package com.example.puriqtours.entity;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Tour implements Serializable {

    private String idTour; // Usar String si el ID proviene del docId de Firestore
    private String title;
    private String desc;
    private String location;
    private String date;        // Guardar como String (formato ISO o yyyy-MM-dd)
    private Float price;
    private String status;
    private String startTime;   // Guardar como String (HH:mm)
    private String endTime;     // Guardar como String (HH:mm)
    private Usuario admin;
    private Integer rating;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }


    // 🔹 Constructor vacío requerido por Firebase
    public Tour() {}

    // 🔹 Constructor opcional
    public Tour(String idTour, String title, String desc, String location, String date,
                Float price, String status, String startTime, String endTime,
                Usuario admin, Integer rating) {
        this.idTour = idTour;
        this.title = title;
        this.desc = desc;
        this.location = location;
        this.date = date;
        this.price = price;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.admin = admin;
        this.rating = rating;
    }

    // 🔹 Getters y setters
    public String getIdTour() {
        return idTour;
    }

    public void setIdTour(String idTour) {
        this.idTour = idTour;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Usuario getAdmin() {
        return admin;
    }

    public void setAdmin(Usuario admin) {
        this.admin = admin;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    // 🔹 Excluir campos si no quieres que se guarden en Firebase
    @Exclude
    public boolean isCompleted() {
        return "COMPLETADO".equalsIgnoreCase(status);
    }
}
