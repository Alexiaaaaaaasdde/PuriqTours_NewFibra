package com.example.puriqtours;

import java.sql.Time;
import java.util.Date;

public class Solicitud {
    private String titulo;
    private String descripcion;
    private int imagenResId;

    private String ciudad;

    private Date fecha;

    private Time horaInicio;

    private Time horaFin;

    private boolean expandido; // controla si está expandido o no

    public Solicitud(String titulo, String descripcion, int imagenResId) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenResId = imagenResId;
        this.expandido = false;
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public int getImagenResId() { return imagenResId; }
    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }
    public Date getFecha() {return fecha;}
    public Time getHoraInicio() {return horaInicio;}
    public Time getHoraFin() {return horaFin;}
    public String getCiudad() {return ciudad;}

}

