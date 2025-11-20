package com.example.puriqtours.entity;

import java.util.List;

public class TourGuia {

    // 🔹 Datos de la reserva
    private String idReserva;
    private String idTour;
    private String idCliente;
    private String idGuia;
    private String status;
    private String tokenInicio;
    private String tokenFin;

    // 🔹 Datos del tour (vienen de /tours)
    private String name;
    private String desc;
    private String location;
    private String date;
    private String startTime;
    private String endTime;
    private String img;
    private Double price;
    private List<String> addedServices;
    private List<CheckpointReserva> checkpoints;
    private boolean expandido;

    public TourGuia() {}

    public TourGuia(String idReserva, String idTour, String idCliente, String idGuia,
                    String status, String tokenInicio, String tokenFin,
                    String name, String desc, String location, String date,
                    String startTime, String endTime, String img, Double price) {

        this.idReserva = idReserva;
        this.idTour = idTour;
        this.idCliente = idCliente;
        this.idGuia = idGuia;
        this.status = status;
        this.tokenInicio = tokenInicio;
        this.tokenFin = tokenFin;

        this.name = name;
        this.desc = desc;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.img = img;
        this.price = price;
    }

    // Getters
    public String getIdReserva() { return idReserva; }
    public String getIdTour() { return idTour; }
    public String getIdCliente() { return idCliente; }
    public String getIdGuia() { return idGuia; }
    public String getStatus() { return status; }
    public String getTokenInicio() { return tokenInicio; }
    public String getTokenFin() { return tokenFin; }

    public String getName() { return name; }
    public String getDesc() { return desc; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImg() { return img; }
    public Double getPrice() { return price; }

    public List<String> getAddedServices() {
        return addedServices;
    }

    public void setAddedServices(List<String> addedServices) {
        this.addedServices = addedServices;
    }

    public List<CheckpointReserva> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(List<CheckpointReserva> checkpoints) { this.checkpoints = checkpoints; }

    public boolean isExpandido() {
        return expandido;
    }

    public void setExpandido(boolean expandido) {
        this.expandido = expandido;
    }
}
