package com.example.puriqtours.entity;

public class Notification {
    private String id;
    private String type; // "tour_finalizado"
    private String tourId;
    private String tourName;
    private String reservaId;
    private String guideName;
    private int clientCount;
    private long timestamp;
    private boolean read;

    public Notification() {
    }

    public Notification(String id, String type, String tourId, String tourName,
                       String reservaId, String guideName, int clientCount, long timestamp) {
        this.id = id;
        this.type = type;
        this.tourId = tourId;
        this.tourName = tourName;
        this.reservaId = reservaId;
        this.guideName = guideName;
        this.clientCount = clientCount;
        this.timestamp = timestamp;
        this.read = false;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTourId() {
        return tourId;
    }

    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getReservaId() {
        return reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public int getClientCount() {
        return clientCount;
    }

    public void setClientCount(int clientCount) {
        this.clientCount = clientCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
