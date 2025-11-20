package com.example.puriqtours.entity;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.List;

public class Tour implements Serializable {

    private String idTour; // Usar String si el ID proviene del docId de Firestore
    private String title;
    private String desc;
    private String location;    // Ubicación específica (ej: "Machu Picchu", "Kuelap")
    private String region;      // Departamento/Región (ej: "Cusco", "Arequipa")
    private Float price;
    private String status;      // "Disponible", "No Disponible"
    private String startTime;   // Guardar como String (HH:mm)
    private String endTime;     // Guardar como String (HH:mm)
    private String idEmpresa;   // UID del admin/empresa que creó el tour
    private String imageUrl;    // URL de la imagen en Firebase Storage
    private Integer rating;
    
    // Campos adicionales para EditTourActivity
    private String idiomas;
    private List<ServicioExtra> serviciosExtras;
    private List<Ubicacion> ruta;

    // Clases internas para servicios extras (subcollection: extraServices)
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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Float getPrice() {
            return price;
        }

        public void setPrice(Float price) {
            this.price = price;
        }
    }

    // Clases internas para ubicaciones (subcollection: locations)
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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }


    // 🔹 Constructor vacío requerido por Firebase
    public Tour() {}

    // 🔹 Constructor opcional
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
