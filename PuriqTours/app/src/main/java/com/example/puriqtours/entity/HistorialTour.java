package com.example.puriqtours.entity;

public class HistorialTour {
    private String idReserva;
    private String idTour;
    private String titulo;
    private String fecha;
    private String hora;
    private String estado;
    private String precio;
    private String viajeros;
    private int imagen;
    private float rating;
    private String imageUrl;

    public HistorialTour(String idTour, String titulo, String fecha, String hora,
                         String estado, String precio, String viajeros, int imagen,
                         float rating, String imageUrl) {
        this.idTour = idTour;
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.precio = precio;
        this.viajeros = viajeros;
        this.imagen = imagen;
        this.rating = rating;
        this.imageUrl = imageUrl;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getViajeros() {
        return viajeros;
    }

    public void setViajeros(String viajeros) {
        this.viajeros = viajeros;
    }

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
