package com.example.puriqtours.entity;

public class CheckpointReserva {

    private String id;       // ID del documento dentro de la subcolección
    private String title;
    private double lat;
    private double lng;
    private String status;   // Pendiente / Visitado
    private int order;       // para ordenar secuencialmente

    public CheckpointReserva() {}

    public CheckpointReserva(String id, String title, double lat, double lng, String status, int order) {
        this.id = id;
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.order = order;
    }

    // GETTERS
    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getStatus() { return status; }
    public int getOrder() { return order; }

    // SETTERS
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setStatus(String status) { this.status = status; }
    public void setOrder(int order) { this.order = order; }
}
