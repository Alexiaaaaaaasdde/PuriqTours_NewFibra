package com.example.puriqtours.entity;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.List;

public class Tour implements Serializable {

    private String idTour; // Usar String si el ID proviene del docId de Firestore
    private String title;
    private String desc;
    private String location;
    private String date;        // Guardar como String (formato ISO o yyyy-MM-dd)
    private Float price;
    private String status;
    private String startTime;   // Guardar como String (HH:mm)
    private String endTime;     // Guardar como String (HH:mm)
    private Usuario admin;
    private Integer rating;
    
    // Campos adicionales para EditTourActivity
    private String idiomas;
    private List<ServicioExtra> serviciosExtras;
    private List<Ubicacion> ruta;

    // Clases internas para servicios extras y ubicaciones
    public static class ServicioExtra implements Serializable {
        private String nombre;
        private String precio;
        private String descripcion;

        public ServicioExtra() {}

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getPrecio() {
            return precio;
        }

        public void setPrecio(String precio) {
            this.precio = precio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }

    public static class Ubicacion implements Serializable {
        private String nombre;
        private String actividades;

        public Ubicacion() {}

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getActividades() {
            return actividades;
        }

        public void setActividades(String actividades) {
            this.actividades = actividades;
        }
    }

    // 🔹 Constructor vacío requerido por Firebase
    public Tour() {}

    // 🔹 Constructor opcional
    public Tour(String idTour, String title, String desc, String location, String date,
                Float price, String status, String startTime, String endTime,
                Usuario admin, Integer rating) {
        this.idTour = idTour;
        this.title = title;
        this.desc = desc;
        this.location = location;
        this.date = date;
        this.price = price;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.admin = admin;
        this.rating = rating;
    }

    // 🔹 Getters y setters
    public String getIdTour() {
        return idTour;
    }

    public void setIdTour(String idTour) {
        this.idTour = idTour;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Usuario getAdmin() {
        return admin;
    }

    public void setAdmin(Usuario admin) {
        this.admin = admin;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(String idiomas) {
        this.idiomas = idiomas;
    }

    public List<ServicioExtra> getServiciosExtras() {
        return serviciosExtras;
    }

    public void setServiciosExtras(List<ServicioExtra> serviciosExtras) {
        this.serviciosExtras = serviciosExtras;
    }

    public List<Ubicacion> getRuta() {
        return ruta;
    }

    public void setRuta(List<Ubicacion> ruta) {
        this.ruta = ruta;
    }

    // 🔹 Excluir campos si no quieres que se guarden en Firebase
    @Exclude
    public boolean isCompleted() {
        return "COMPLETADO".equalsIgnoreCase(status);
    }
}
