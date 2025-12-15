package com.example.puriqtours.entity;

import java.util.List;

public class TourGuia {

    // 🔹 Datos de la reserva
    private String idReserva;
    private String idTour;
    private String idGuia;
    private String status;
    private String date;
    private Long totalClients;
    private Long verifiedClients;
    private List<CheckpointReserva> checkpoints;
    private List<ReservaIndividual> reservaIndividual;
    private boolean expandido;
    // 🔹 Datos del tour (vienen de /tours)
    private String name;
    private String desc;
    private String location;
    private String startTime;
    private String endTime;
    private String img;


    public TourGuia() {}

    public TourGuia(String idReserva, String idTour, String idGuia, String status,
                    String date, Long totalClients, Long verifiedClients,
                    String name, String desc, String location,
                    String startTime, String endTime, String img) {

        this.idReserva = idReserva;
        this.idTour = idTour;
        this.idGuia = idGuia;
        this.status = status;
        this.name = name;
        this.desc = desc;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.img = img;
        this.totalClients = totalClients;
        this.verifiedClients = verifiedClients;
    }


    // Getters
    public String getIdReserva() { return idReserva; }
    public String getIdTour() { return idTour; }
    public String getIdGuia() { return idGuia; }
    public String getStatus() { return status; }

    public String getName() { return name; }
    public String getDesc() { return desc; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImg() { return img; }

    public List<CheckpointReserva> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(List<CheckpointReserva> checkpoints) { this.checkpoints = checkpoints; }

    public boolean isExpandido() {
        return expandido;
    }

    public void setExpandido(boolean expandido) {
        this.expandido = expandido;
    }

    public Long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(Long totalClients) {
        this.totalClients = totalClients;
    }

    public Long getVerifiedClients() {
        return verifiedClients;
    }

    public void setVerifiedClients(Long verifiedClients) {
        this.verifiedClients = verifiedClients;
    }

    public List<ReservaIndividual> getReservaIndividual() {
        return reservaIndividual;
    }

    public void setReservaIndividual(List<ReservaIndividual> reservaIndividual) {
        this.reservaIndividual = reservaIndividual;
    }
}
