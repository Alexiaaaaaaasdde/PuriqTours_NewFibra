package com.example.puriqtours.admin;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class RouteLocation {
    private String title;
    private LatLng latLng;
    private int order;
    private Marker marker;

    public RouteLocation() {
    }

    public RouteLocation(String title, LatLng latLng, int order) {
        this.title = title;
        this.latLng = latLng;
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public double getLat() {
        return latLng != null ? latLng.latitude : 0;
    }

    public double getLng() {
        return latLng != null ? latLng.longitude : 0;
    }
}
