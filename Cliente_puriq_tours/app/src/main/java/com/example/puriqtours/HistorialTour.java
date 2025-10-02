package com.example.puriqtours;

public class HistorialTour {
    private String titulo;
    private String ubicacion;
    private String estado;
    private String duracion;
    private int rating;
    private String precio;
    private int imagenResId;

    public HistorialTour(String titulo, String ubicacion, String estado, String duracion, int rating, String precio, int imagenResId) {
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.duracion = duracion;
        this.rating = rating;
        this.precio = precio;
        this.imagenResId = imagenResId;
    }

    // getters
    public String getTitulo() { return titulo; }
    public String getUbicacion() { return ubicacion; }
    public String getEstado() { return estado; }
    public String getDuracion() { return duracion; }
    public int getRating() { return rating; }
    public String getPrecio() { return precio; }
    public int getImagenResId() { return imagenResId; }
}
