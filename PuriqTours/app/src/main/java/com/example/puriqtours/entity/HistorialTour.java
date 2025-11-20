package com.example.puriqtours.entity;

import java.io.Serializable;

public class HistorialTour implements Serializable {

    private String idReserva;     // ID del documento en 'reservas'
    private String idTour;
    private String titulo;
    private String fecha;
    private String hora;
    private String estado;
    private String precio;
    private String viajeros;

    private int imagenResId;      // recurso local por defecto
    private float rating;
    private String imageUrl;      // URL de la imagen real

    // 🔹 Constructor vacío obligatorio para Firebase
    public HistorialTour() {}

    // 🔹 Constructor usado en HistorialActivity
    public HistorialTour(String idTour,
                         String titulo,
                         String fecha,
                         String hora,
                         String estado,
                         String precio,
                         String viajeros,
                         int imagenResId,
                         float rating,
                         String imageUrl) {

        this.idTour = idTour;
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.precio = precio;
        this.viajeros = viajeros;
        this.imagenResId = imagenResId;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    // 🔹 Setters y getters
    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }

    public String getIdTour() { return idTour; }
    public String getTitulo() { return titulo; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }
    public String getPrecio() { return precio; }
    public String getViajeros() { return viajeros; }
    public int getImagenResId() { return imagenResId; }
    public float getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
