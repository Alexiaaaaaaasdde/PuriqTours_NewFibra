package com.example.puriqtours.helper;

import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.entity.TourAdmin;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper para convertir entre Tour (Firestore) y TourAdmin (UI/Adapter)
 */
public class TourConverter {
    
    /**
     * Convertir Tour (Firestore) a TourAdmin (para adapters/UI)
     */
    public static TourAdmin tourToTourAdmin(Tour tour) {
        if (tour == null) return null;
        
        TourAdmin tourAdmin = new TourAdmin();
        
        // Usar el ID directamente como String
        tourAdmin.setId(tour.getIdTour() != null ? tour.getIdTour() : "");
        
        tourAdmin.setName(tour.getTitle() != null ? tour.getTitle() : "Sin nombre");
        tourAdmin.setDescription(tour.getDesc() != null ? tour.getDesc() : "Sin descripción");
        tourAdmin.setLocation(tour.getLocation() != null ? tour.getLocation() : "Sin ubicación");
        tourAdmin.setRegion(tour.getRegion() != null ? tour.getRegion() : "Sin región");
        tourAdmin.setPrice(tour.getPrice() != null ? tour.getPrice().doubleValue() : 0.0);
        tourAdmin.setDuration(1); // Duración por defecto
        tourAdmin.setGuideAssigned(""); // Se puede obtener del campo guia si existe
        tourAdmin.setImageResource(android.R.drawable.ic_menu_gallery); // Imagen por defecto
        tourAdmin.setImageUrl(tour.getImageUrl()); // URL de Firebase Storage
        tourAdmin.setAvailable(true);
        
        return tourAdmin;
    }
    
    /**
     * Convertir TourAdmin a Tour (para guardar en Firestore)
     */
    public static Tour tourAdminToTour(TourAdmin tourAdmin) {
        if (tourAdmin == null) return null;
        
        Tour tour = new Tour();
        
        tour.setIdTour(tourAdmin.getId());
        tour.setTitle(tourAdmin.getName());
        tour.setDesc(tourAdmin.getDescription());
        tour.setLocation(tourAdmin.getLocation());
        tour.setRegion(tourAdmin.getRegion());
        tour.setPrice(tourAdmin.getPrice() != 0 ? (float) tourAdmin.getPrice() : null);
        tour.setStatus(tourAdmin.isAvailable() ? "disponible" : "no disponible");
        
        return tour;
    }
    
    /**
     * Convertir lista de Tours a lista de TourAdmins
     */
    public static List<TourAdmin> toursToTourAdmins(List<Tour> tours) {
        List<TourAdmin> tourAdmins = new ArrayList<>();
        if (tours != null) {
            for (Tour tour : tours) {
                TourAdmin tourAdmin = tourToTourAdmin(tour);
                if (tourAdmin != null) {
                    tourAdmins.add(tourAdmin);
                }
            }
        }
        return tourAdmins;
    }
    
    /**
     * Convertir lista de TourAdmins a lista de Tours
     */
    public static List<Tour> tourAdminsToTours(List<TourAdmin> tourAdmins) {
        List<Tour> tours = new ArrayList<>();
        if (tourAdmins != null) {
            for (TourAdmin tourAdmin : tourAdmins) {
                Tour tour = tourAdminToTour(tourAdmin);
                if (tour != null) {
                    tours.add(tour);
                }
            }
        }
        return tours;
    }
}
