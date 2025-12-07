package com.example.puriqtours.entity;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.List;

public class Tour implements Serializable {

    private String idTour;
    private String title;
    private String desc;
    private String location;
    private String region;
    private Float price;
    private String status;
    private String startTime;
    private String endTime;
    private String idEmpresa;
    private String imageUrl;
    private Integer rating;

    // 🔹 LISTA DE HORARIOS DISPONIBLES
    private List<String> horarios;

    // Campos adicionales
    private String idiomas;
    private List<ServicioExtra> serviciosExtras;
    private List<Ubicacion> ruta;

    // Clases internas
    public static class ServicioExtra implements Serializable {
        private String title;
        private String imageUrl;
        private Float price;

        public ServicioExtra() {}

        public ServicioExtra(String title, String imageUrl, Float price) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.price = price;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public Float getPrice() { return price; }
        public void setPrice(Float price) { this.price = price; }
    }

    public static class Ubicacion implements Serializable {
        private String title;
        private Integer order;
        private Double lat;
        private Double lng;

        public Ubicacion() {}

        public Ubicacion(String title, Integer order, Double lat, Double lng) {
            this.title = title;
            this.order = order;
            this.lat = lat;
            this.lng = lng;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
    }

    // Constructores
    public Tour() {}

    public Tour(String idTour, String title, String desc, String location, String region,
                Float price, String status, String startTime, String endTime,
                String idEmpresa, String imageUrl, Integer rating) {
        this.idTour = idTour;
        this.title = title;
        this.desc = desc;
        this.location = location;
        this.region = region;
        this.price = price;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.idEmpresa = idEmpresa;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    // Getters y Setters
    public String getIdTour() { return idTour; }
    public void setIdTour(String idTour) { this.idTour = idTour; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(String idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    // 🔹 GETTERS Y SETTERS PARA HORARIOS
    public List<String> getHorarios() { return horarios; }
    public void setHorarios(List<String> horarios) { this.horarios = horarios; }

    public String getIdiomas() { return idiomas; }
    public void setIdiomas(String idiomas) { this.idiomas = idiomas; }

    public List<ServicioExtra> getServiciosExtras() { return serviciosExtras; }
    public void setServiciosExtras(List<ServicioExtra> serviciosExtras) { this.serviciosExtras = serviciosExtras; }

    public List<Ubicacion> getRuta() { return ruta; }
    public void setRuta(List<Ubicacion> ruta) { this.ruta = ruta; }

    @Exclude
    public boolean isCompleted() {
        return "COMPLETADO".equalsIgnoreCase(status);
    }
}