package com.example.puriqtours;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;

public class Solicitud {
    private String titulo;
    private String descripcion;
    private int imagenResId;

    private String ciudad;

    private String fecha;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private String empresa;

    private boolean expandido; // controla si está expandido o no

    public Solicitud(String titulo, String descripcion, int imagenResId, String ciudad, String fecha, LocalTime horaInicio, LocalTime horaFin, String empresa) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenResId = imagenResId;
        this.ciudad = ciudad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.empresa = empresa;
        this.expandido = false;
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public int getImagenResId() { return imagenResId; }
    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }
    public String getFecha() {return fecha;}
    public LocalTime getHoraInicio() {return horaInicio;}
    public LocalTime getHoraFin() {return horaFin;}
    public String getCiudad() {return ciudad;}

    public String getEmpresa() {return empresa;}

}

