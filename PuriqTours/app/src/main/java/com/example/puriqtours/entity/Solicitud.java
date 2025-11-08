package com.example.puriqtours.entity;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Solicitud {
    private String titulo;
    private String descripcion;
    private String imagenUrl;   // ahora usaremos URL en lugar de resourceId
    private String ciudad;
    private String fecha;       // puedes mantenerlo como String (ej. "2025-11-06")
    private String horaInicio; // compatible con Firestore
    private String horaFin;
    private String empresa;
    private boolean expandido;

    // 🔹 Constructor vacío (requerido por Firebase)
    public Solicitud() {}

    // 🔹 Constructor completo
    public Solicitud(String titulo, String descripcion, String imagenUrl,
                     String ciudad, String fecha, String horaInicio,
                     String horaFin, String empresa) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.ciudad = ciudad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.empresa = empresa;
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }

    // 🔹 Método opcional para convertir a Map (útil para guardar en Firestore)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("titulo", titulo);
        map.put("descripcion", descripcion);
        map.put("imagenUrl", imagenUrl);
        map.put("ciudad", ciudad);
        map.put("fecha", fecha);
        map.put("horaInicio", horaInicio);
        map.put("horaFin", horaFin);
        map.put("empresa", empresa);
        return map;
    }
}
