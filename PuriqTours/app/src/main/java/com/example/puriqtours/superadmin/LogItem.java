package com.example.puriqtours.superadmin;

public class LogItem {
    public String tipo;
    public String fecha;
    public String descripcion;
    public long timestamp; // En milisegundos para ordenar

    public LogItem(String tipo, String fecha, String descripcion) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.timestamp = 0;
    }
}
