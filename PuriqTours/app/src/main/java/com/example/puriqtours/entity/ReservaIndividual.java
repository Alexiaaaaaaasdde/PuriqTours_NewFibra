package com.example.puriqtours.entity;

import java.util.List;

public class ReservaIndividual {

    private String idReservaIndividual;
    private String idCliente;
    private Double price;
    private String tokenStart;
    private String tokenEnd;
    private String travelers;
    private String codigoOperacion;
    private String metodoPago;
    private boolean verified;
    private boolean finished;
    private List<String> addedServices;

    public ReservaIndividual() {
    }

    public ReservaIndividual(String idCliente, Double price, String tokenStart, String tokenEnd, String travelers,
                              String codigoOperacion, String metodoPago, boolean verified, boolean finished, List<String> addedServices){
        this.setIdCliente(idCliente);
        this.setPrice(price);
        this.setTokenStart(tokenStart);
        this.setTokenEnd(tokenEnd);
        this.setTravelers(travelers);
        this.setCodigoOperacion(codigoOperacion);
        this.setMetodoPago(metodoPago);
        this.setVerified(verified);
        this.setFinished(finished);
        this.setAddedServices(addedServices);
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTokenStart() {
        return tokenStart;
    }

    public void setTokenStart(String tokenStart) {
        this.tokenStart = tokenStart;
    }

    public String getTokenEnd() {
        return tokenEnd;
    }

    public void setTokenEnd(String tokenEnd) {
        this.tokenEnd = tokenEnd;
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

    public String getIdReservaIndividual() {
        return idReservaIndividual;
    }

    public void setIdReservaIndividual(String idReservaIndividual) {
        this.idReservaIndividual = idReservaIndividual;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
