package com.example.puriqtours.entity;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.List;

public class ReservaCliente implements Serializable {

    private String idReserva;   // ID del documento en Firestore
    private String idTour;      // Referencia al tour reservado
    private String idGuia;      // UID del guía asignado
    private String title;       // Nombre/título de la reserva
    private String status;      // "Reservado", "En proceso", "Finalizado"
    private String hour;        // Hora de inicio (formato: "HH:mm")
    private String date;        // Fecha de la reserva (formato: "yyyy-MM-dd")
    private Long totalClients;
    private Long verifiedClients;
    private Long finishedClients;
    private List<ReservaIndividual> reservaIndividualList;


    // Clases internas para subcollections

    // Constructor vacío requerido por Firestore
    public ReservaCliente() {}

    // Constructor completo
    public ReservaCliente(String idReserva, String idTour, String idGuia, String title, String hour,
                   String date, String status, Long totalClients, Long verifiedClients, Long finishedClients) {
        this.idReserva = idReserva;
        this.idTour = idTour;
        this.idGuia = idGuia;
        this.title = title;
        this.status = status;
        this.hour = hour;
        this.date = date;
        this.setTotalClients(totalClients);
        this.setVerifiedClients(verifiedClients);
        this.setFinishedClients(finishedClients);
    }

    // Getters y Setters
    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public String getIdTour() {
        return idTour;
    }

    public void setIdTour(String idTour) {
        this.idTour = idTour;
    }

    public String getIdGuia() {
        return idGuia;
    }

    public void setIdGuia(String idGuia) {
        this.idGuia = idGuia;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Exclude
    public boolean isCompleted() {
        return "Finalizado".equalsIgnoreCase(status);
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

    public Long getFinishedClients() {
        return finishedClients;
    }

    public void setFinishedClients(Long finishedClients) {
        this.finishedClients = finishedClients;
    }
}
