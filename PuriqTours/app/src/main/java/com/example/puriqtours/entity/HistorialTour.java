package com.example.puriqtours.entity;

import java.io.Serializable;

public class HistorialTour implements Serializable {

    private String idTour;
    private String titulo;
    private String fecha;
    private String hora;
    private String estado;
    private String precio;
    private String viajeros;

    private int imagenResId = 0;  // opcional

    public HistorialTour() {}

    public HistorialTour(String idTour, String titulo, String fecha, String hora,
                         String estado, String precio, String viajeros, int imagenResId) {
        this.idTour = idTour;
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.precio = precio;
        this.viajeros = viajeros;
        this.imagenResId = imagenResId;
    }

    public String getIdTour() { return idTour; }
    public String getTitulo() { return titulo; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }
    public String getPrecio() { return precio; }
    public String getViajeros() { return viajeros; }
    public int getImagenResId() { return imagenResId; }
}
