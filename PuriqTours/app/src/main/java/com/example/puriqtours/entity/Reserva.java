package com.example.puriqtours.entity;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.List;

public class Reserva implements Serializable {

    private String idReserva;   // ID del documento en Firestore
    private String idTour;      // Referencia al tour reservado
    private String idCliente;   // UID del cliente que hizo la reserva
    private String idGuia;      // UID del guía asignado
    private String title;       // Nombre/título de la reserva
    private String status;      // "Reservado", "En proceso", "Finalizado"
    private String hour;        // Hora de inicio (formato: "HH:mm")
    private String date;        // Fecha de la reserva (formato: "yyyy-MM-dd")
    private Float price;        // Precio total (tour + servicios extra)
    private String qrStart;     // Token QR para inicio
    private String qrEnd;       // Token QR para fin
    private String travelers;   // Formato: "x adultos, y niños"
    private String metodoPago;  // Método de pago utilizado
    private String codigoOperacion; // Código de operación del pago
    
    // Campos adicionales del tour (para filtrado)
    private String tourRegion;  // Región/departamento del tour
    private String tourLocation; // Ubicación específica del tour

    // Subcollections (no se guardan directamente en el documento)
    // addedServices (subcollection) - servicios extras agregados a la reserva
    // checkpoints (subcollection) - puntos de control del recorrido

    // Clases internas para subcollections
    public static class AddedService implements Serializable {
        private String title;
        private String imageUrl;

        public AddedService() {}

        public AddedService(String title, String imageUrl) {
            this.title = title;
            this.imageUrl = imageUrl;
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
    }

    public static class Checkpoint implements Serializable {
        private String title;
        private Integer order;
        private Double lat;
        private Double lng;
        private String status;  // "Pendiente", "Visitado"

        public Checkpoint() {}

        public Checkpoint(String title, Integer order, Double lat, Double lng, String status) {
            this.title = title;
            this.order = order;
            this.lat = lat;
            this.lng = lng;
            this.status = status;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // Constructor vacío requerido por Firestore
    public Reserva() {}

    // Constructor completo
    public Reserva(String idReserva, String idTour, String idCliente, String idGuia,
                   String title, String status, String hour, String date, Float price,
                   String qrStart, String qrEnd, String travelers, String metodoPago,
                   String codigoOperacion) {
        this.idReserva = idReserva;
        this.idTour = idTour;
        this.idCliente = idCliente;
        this.idGuia = idGuia;
        this.title = title;
        this.status = status;
        this.hour = hour;
        this.date = date;
        this.price = price;
        this.qrStart = qrStart;
        this.qrEnd = qrEnd;
        this.travelers = travelers;
        this.metodoPago = metodoPago;
        this.codigoOperacion = codigoOperacion;
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

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdGuia() {
        return idGuia;
    }

    public void setIdGuia(String idGuia) {
        this.idGuia = idGuia;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
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

    public String getQrStart() {
        return qrStart;
    }

    public void setQrStart(String qrStart) {
        this.qrStart = qrStart;
    }

    public String getQrEnd() {
        return qrEnd;
    }

    public void setQrEnd(String qrEnd) {
        this.qrEnd = qrEnd;
    }

    public String getTravelers() {
        return travelers;
    }

    public void setTravelers(String travelers) {
        this.travelers = travelers;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public void setCodigoOperacion(String codigoOperacion) {
        this.codigoOperacion = codigoOperacion;
    }

    public String getTourRegion() {
        return tourRegion;
    }

    public void setTourRegion(String tourRegion) {
        this.tourRegion = tourRegion;
    }

    public String getTourLocation() {
        return tourLocation;
    }

    public void setTourLocation(String tourLocation) {
        this.tourLocation = tourLocation;
    }

    @Exclude
    public boolean isCompleted() {
        return "Finalizado".equalsIgnoreCase(status);
    }
}
