package com.example.puriqtours.entity;

import com.google.firebase.firestore.DocumentSnapshot;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Review implements Serializable {

    private String idReview;
    private String idReserva;
    private String idTour;
    private String idCliente;
    private String idGuia;
    private String clienteName;
    private String clienteProfileImage;

    // Ratings individuales
    private float ratingTour;          // Calificación del tour
    private float ratingServicios;     // Calificación de servicios
    private float ratingGuia;          // Calificación del guía

    private String comentario;
    private String fecha;              // Fecha de la valoración
    private String tourTitle;          // Título del tour valorado
    private String tourLocation;       // Ubicación del tour

    // Constructor vacío (requerido por Firestore)
    public Review() {}

    // Constructor completo
    public Review(String idReview, String idReserva, String idTour, String idCliente,
                  String idGuia, String clienteName, String clienteProfileImage,
                  float ratingTour, float ratingServicios, float ratingGuia,
                  String comentario, String fecha, String tourTitle, String tourLocation) {
        this.idReview = idReview;
        this.idReserva = idReserva;
        this.idTour = idTour;
        this.idCliente = idCliente;
        this.idGuia = idGuia;
        this.clienteName = clienteName;
        this.clienteProfileImage = clienteProfileImage;
        this.ratingTour = ratingTour;
        this.ratingServicios = ratingServicios;
        this.ratingGuia = ratingGuia;
        this.comentario = comentario;
        this.fecha = fecha;
        this.tourTitle = tourTitle;
        this.tourLocation = tourLocation;
    }

    // Getters y Setters
    public String getIdReview() { return idReview; }
    public void setIdReview(String idReview) { this.idReview = idReview; }

    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }

    public String getIdTour() { return idTour; }
    public void setIdTour(String idTour) { this.idTour = idTour; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getIdGuia() { return idGuia; }
    public void setIdGuia(String idGuia) { this.idGuia = idGuia; }

    public String getClienteName() { return clienteName; }
    public void setClienteName(String clienteName) { this.clienteName = clienteName; }

    public String getClienteProfileImage() { return clienteProfileImage; }
    public void setClienteProfileImage(String clienteProfileImage) {
        this.clienteProfileImage = clienteProfileImage;
    }

    public float getRatingTour() { return ratingTour; }
    public void setRatingTour(float ratingTour) { this.ratingTour = ratingTour; }

    public float getRatingServicios() { return ratingServicios; }
    public void setRatingServicios(float ratingServicios) {
        this.ratingServicios = ratingServicios;
    }

    public float getRatingGuia() { return ratingGuia; }
    public void setRatingGuia(float ratingGuia) { this.ratingGuia = ratingGuia; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }

    public String getTourLocation() { return tourLocation; }
    public void setTourLocation(String tourLocation) { this.tourLocation = tourLocation; }

    // Calcular rating promedio
    public float getRatingPromedio() {
        return (ratingTour + ratingServicios + ratingGuia) / 3.0f;
    }

    // Mapeo a Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("idReserva", idReserva);
        map.put("idTour", idTour);
        map.put("idCliente", idCliente);
        map.put("idGuia", idGuia);
        map.put("clienteName", clienteName);
        map.put("clienteProfileImage", clienteProfileImage);
        map.put("ratingTour", ratingTour);
        map.put("ratingServicios", ratingServicios);
        map.put("ratingGuia", ratingGuia);
        map.put("comentario", comentario);
        map.put("fecha", fecha);
        map.put("tourTitle", tourTitle);
        map.put("tourLocation", tourLocation);
        return map;
    }

    // Crear desde DocumentSnapshot
    public static Review fromSnapshot(DocumentSnapshot doc) {
        Review review = doc.toObject(Review.class);
        if (review != null) {
            review.setIdReview(doc.getId());
        }
        return review;
    }
}
