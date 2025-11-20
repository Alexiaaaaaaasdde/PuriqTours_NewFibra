package com.example.puriqtours.entity;

import com.google.firebase.firestore.DocumentSnapshot;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Entidad Solicitud según la documentación de Firestore
 * Representa una propuesta de reserva enviada por un admin a un guía
 */
public class Solicitud implements Serializable {
    
    private String id;           // ID del documento en Firestore
    private String title;        // Título de la solicitud
    private String desc;         // Descripción de la solicitud
    private Number pay;          // Paga que se le dará al guía
    private String idGuia;       // UID del guía al que se le propone
    private String idEmpresa;    // UID del admin/empresa que propone
    private String idReserva;    // ID de la reserva que se propone
    private String status;       // "Pendiente", "Aceptado", "Rechazado"

    // Constructor vacío (necesario para Firestore)
    public Solicitud() {}

    // Constructor completo
    public Solicitud(String id, String title, String desc, Number pay, 
                    String idGuia, String idEmpresa, String idReserva, String status) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.pay = pay;
        this.idGuia = idGuia;
        this.idEmpresa = idEmpresa;
        this.idReserva = idReserva;
        this.status = status;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public Number getPay() { return pay; }
    public void setPay(Number pay) { this.pay = pay; }

    public String getIdGuia() { return idGuia; }
    public void setIdGuia(String idGuia) { this.idGuia = idGuia; }

    public String getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(String idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Mapeo a Firestore
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

    // Crear instancia desde DocumentSnapshot
    public static Solicitud fromSnapshot(DocumentSnapshot doc) {
        Solicitud s = doc.toObject(Solicitud.class);
        if (s != null) {
            s.setId(doc.getId());
        }
        return s;
    }
}

