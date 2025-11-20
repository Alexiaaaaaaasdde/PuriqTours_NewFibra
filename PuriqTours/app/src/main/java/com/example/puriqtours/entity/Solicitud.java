package com.example.puriqtours.entity;

import java.util.HashMap;
import java.util.Map;

public class Solicitud {

    private String idSolicitud;
    private String title;      // título de la solicitud
    private String desc;       // descripción
    private double pay;        // paga del guía
    private String idGuia;     // guía que recibirá la solicitud
    private String idEmpresa;  // empresa que envía la solicitud
    private String idReserva;     // tour asociado
    private String status;     // Pendiente, Aceptado, Rechazado
    private String imageUrl;

    private boolean expandido; // Para animaciones en tu adapter

    // 🔹 Constructor vacío (Firebase lo necesita)
    public Solicitud() {}

    // 🔹 Constructor completo
    public Solicitud(String idSolicitud, String title, String desc, double pay,
                     String idGuia, String idEmpresa, String idReserva,
                     String status) {
        this.setIdSolicitud(idSolicitud);
        this.title = title;
        this.desc = desc;
        this.pay = pay;
        this.idGuia = idGuia;
        this.idEmpresa = idEmpresa;
        this.idReserva = idReserva;
        this.status = status;
    }

    // -----------------
    // GETTERS & SETTERS
    // -----------------

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public double getPay() { return pay; }
    public void setPay(double pay) { this.pay = pay; }

    public String getIdGuia() { return idGuia; }
    public void setIdGuia(String idGuia) { this.idGuia = idGuia; }

    public String getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(String idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idTour) { this.idReserva = idTour; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }


    // -----------------------
    // 🔹 Convertir a Firebase
    // -----------------------
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("desc", desc);
        map.put("pay", pay);
        map.put("idGuia", idGuia);
        map.put("idEmpresa", idEmpresa);
        map.put("idReserva", idReserva);
        map.put("status", status);
        return map;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(String idSolicitud) {
        this.idSolicitud = idSolicitud;
    }
}
