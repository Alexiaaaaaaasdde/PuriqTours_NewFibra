package com.example.puriqtours.entity;

import java.util.List;

public class ReservaIndividual {
    private String idCliente;
    private Number price;
    private String qrStart;
    private String qrEnd;
    private String travelers;
    private String codigoOperacion;
    private String metodoPago;
    private List<String> addedServices;

    public ReservaIndividual(String idCliente, Number price, String qrStart, String qrEnd,
                             String travelers, String codigoOperacion, String metodoPago, List<String> addedServices){
        this.setIdCliente(idCliente);
        this.setPrice(price);
        this.setQrStart(qrStart);
        this.setQrEnd(qrEnd);
        this.setTravelers(travelers);
        this.setCodigoOperacion(codigoOperacion);
        this.setMetodoPago(metodoPago);
        this.setAddedServices(addedServices);
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public Number getPrice() {
        return price;
    }

    public void setPrice(Number price) {
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

    public List<String> getAddedServices() {
        return addedServices;
    }

    public void setAddedServices(List<String> addedServices) {
        this.addedServices = addedServices;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public void setCodigoOperacion(String codigoOperacion) {
        this.codigoOperacion = codigoOperacion;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
