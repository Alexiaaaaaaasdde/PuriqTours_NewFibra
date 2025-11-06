package com.example.puriqtours.helper;

import android.util.Log;

import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.entity.Usuario;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class para manejar todas las operaciones de Firestore
 * Solo para el ROL ADMIN
 */
public class FirestoreHelper {
    
    private static final String TAG = "FirestoreHelper";
    private final FirebaseFirestore db;
    
    // Nombres de colecciones
    private static final String COLLECTION_TOURS = "tours";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_EMPRESAS = "empresas";
    
    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }
    
    // ==================== TOURS ====================
    
    /**
     * Cargar todos los tours de Firestore
     */
    public void loadTours(OnToursLoadedListener listener) {
        db.collection(COLLECTION_TOURS)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Tour> tours = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Tour tour = documentToTour(document);
                    if (tour != null) {
                        tours.add(tour);
                    }
                }
                Log.d(TAG, "Tours cargados: " + tours.size());
                listener.onToursLoaded(tours);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar tours", e);
                listener.onToursLoaded(new ArrayList<>());
            });
    }
    
    /**
     * Cargar un tour específico por ID
     */
    public void loadTourById(String tourId, OnTourLoadedListener listener) {
        db.collection(COLLECTION_TOURS)
            .document(tourId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Tour tour = documentToTour(documentSnapshot);
                    listener.onTourLoaded(tour);
                } else {
                    listener.onTourLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar tour", e);
                listener.onTourLoaded(null);
            });
    }
    
    /**
     * Crear un nuevo tour en Firestore
     */
    public void createTour(Tour tour, OnTourCreatedListener listener) {
        Map<String, Object> tourData = tourToMap(tour);
        
        db.collection(COLLECTION_TOURS)
            .add(tourData)
            .addOnSuccessListener(documentReference -> {
                String tourId = documentReference.getId();
                tour.setIdTour(tourId);
                Log.d(TAG, "Tour creado con ID: " + tourId);
                listener.onTourCreated(true, tourId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al crear tour", e);
                listener.onTourCreated(false, null);
            });
    }
    
    /**
     * Actualizar un tour existente
     */
    public void updateTour(String tourId, Tour tour, OnTourUpdatedListener listener) {
        Map<String, Object> tourData = tourToMap(tour);
        
        db.collection(COLLECTION_TOURS)
            .document(tourId)
            .update(tourData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Tour actualizado: " + tourId);
                listener.onTourUpdated(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar tour", e);
                listener.onTourUpdated(false);
            });
    }
    
    /**
     * Eliminar un tour de Firestore
     */
    public void deleteTour(String tourId, OnTourDeletedListener listener) {
        db.collection(COLLECTION_TOURS)
            .document(tourId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Tour eliminado: " + tourId);
                listener.onTourDeleted(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al eliminar tour", e);
                listener.onTourDeleted(false);
            });
    }
    
    // ==================== GUÍAS ====================
    
    /**
     * Cargar todos los guías (usuarios con rol "Guia")
     */
    public void loadGuides(OnGuidesLoadedListener listener) {
        db.collection(COLLECTION_USERS)
            .whereEqualTo("rol", "Guia")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Usuario> guides = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Usuario guide = Usuario.fromSnapshot(document);
                    if (guide != null) {
                        guides.add(guide);
                    }
                }
                Log.d(TAG, "Guías cargados: " + guides.size());
                listener.onGuidesLoaded(guides);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar guías", e);
                listener.onGuidesLoaded(new ArrayList<>());
            });
    }
    
    /**
     * Cargar un guía específico por ID
     */
    public void loadGuideById(String guideId, OnGuideLoadedListener listener) {
        db.collection(COLLECTION_USERS)
            .document(guideId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Usuario guide = Usuario.fromSnapshot(documentSnapshot);
                    listener.onGuideLoaded(guide);
                } else {
                    listener.onGuideLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar guía", e);
                listener.onGuideLoaded(null);
            });
    }
    
    // ==================== PERFIL ADMIN ====================
    
    /**
     * Cargar perfil del admin actual (usuario logueado)
     */
    public void loadAdminProfile(String adminUid, OnAdminProfileLoadedListener listener) {
        db.collection(COLLECTION_USERS)
            .document(adminUid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Usuario admin = Usuario.fromSnapshot(documentSnapshot);
                    Log.d(TAG, "Perfil admin cargado: " + admin.getName());
                    listener.onAdminProfileLoaded(admin);
                } else {
                    Log.w(TAG, "Perfil admin no encontrado");
                    listener.onAdminProfileLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar perfil admin", e);
                listener.onAdminProfileLoaded(null);
            });
    }
    
    /**
     * Actualizar perfil del admin
     */
    public void updateAdminProfile(String adminUid, String name, String phone, String email, String address, OnAdminProfileUpdatedListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("email", email);
        updates.put("address", address);
        
        db.collection(COLLECTION_USERS)
            .document(adminUid)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Perfil admin actualizado");
                listener.onAdminProfileUpdated(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar perfil admin", e);
                listener.onAdminProfileUpdated(false);
            });
    }
    
    // ==================== EMPRESAS ====================
    
    /**
     * Cargar información de una empresa por ID
     */
    public void loadEmpresaById(String empresaId, OnEmpresaLoadedListener listener) {
        db.collection(COLLECTION_EMPRESAS)
            .document(empresaId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombre = documentSnapshot.getString("nombre");
                    listener.onEmpresaLoaded(nombre);
                } else {
                    listener.onEmpresaLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar empresa", e);
                listener.onEmpresaLoaded(null);
            });
    }
    
    // ==================== CONVERSIÓN DE DATOS ====================
    
    /**
     * Convertir DocumentSnapshot a Tour
     */
    private Tour documentToTour(DocumentSnapshot document) {
        try {
            Tour tour = new Tour();
            tour.setIdTour(document.getId());
            
            // Mapear campos de Firestore a Tour
            if (document.contains("nombre")) {
                tour.setTitle(document.getString("nombre"));
            }
            if (document.contains("descripcion")) {
                tour.setDesc(document.getString("descripcion"));
            }
            if (document.contains("ubicacion")) {
                tour.setLocation(document.getString("ubicacion"));
            }
            if (document.contains("inicioTour")) {
                // inicioTour puede ser Timestamp o String
                Object inicioTourObj = document.get("inicioTour");
                if (inicioTourObj instanceof com.google.firebase.Timestamp) {
                    com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) inicioTourObj;
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault());
                    tour.setDate(sdf.format(timestamp.toDate()));
                } else if (inicioTourObj instanceof String) {
                    tour.setDate((String) inicioTourObj);
                }
            }
            if (document.contains("costo")) {
                Object costoObj = document.get("costo");
                if (costoObj instanceof Number) {
                    tour.setPrice(((Number) costoObj).floatValue());
                }
            }
            if (document.contains("estado")) {
                tour.setStatus(document.getString("estado"));
            }
            if (document.contains("horaInicio")) {
                tour.setStartTime(document.getString("horaInicio"));
            }
            if (document.contains("horaFin")) {
                tour.setEndTime(document.getString("horaFin"));
            }
            if (document.contains("idiomas")) {
                tour.setIdiomas(document.getString("idiomas"));
            }
            
            // Mapear servicios extras
            if (document.contains("serviciosExtras")) {
                List<Map<String, Object>> serviciosMap = (List<Map<String, Object>>) document.get("serviciosExtras");
                if (serviciosMap != null) {
                    List<Tour.ServicioExtra> servicios = new java.util.ArrayList<>();
                    for (Map<String, Object> servicioMap : serviciosMap) {
                        Tour.ServicioExtra servicio = new Tour.ServicioExtra();
                        servicio.setNombre((String) servicioMap.get("nombre"));
                        servicio.setPrecio((String) servicioMap.get("precio"));
                        servicio.setDescripcion((String) servicioMap.get("descripcion"));
                        servicios.add(servicio);
                    }
                    tour.setServiciosExtras(servicios);
                }
            }
            
            // Mapear ruta/ubicaciones
            if (document.contains("ruta")) {
                List<Map<String, Object>> rutaMap = (List<Map<String, Object>>) document.get("ruta");
                if (rutaMap != null) {
                    List<Tour.Ubicacion> ruta = new java.util.ArrayList<>();
                    for (Map<String, Object> ubicacionMap : rutaMap) {
                        Tour.Ubicacion ubicacion = new Tour.Ubicacion();
                        ubicacion.setNombre((String) ubicacionMap.get("nombre"));
                        ubicacion.setActividades((String) ubicacionMap.get("actividades"));
                        ruta.add(ubicacion);
                    }
                    tour.setRuta(ruta);
                }
            }
            
            return tour;
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir documento a Tour", e);
            return null;
        }
    }
    
    /**
     * Convertir Tour a Map para Firestore
     */
    private Map<String, Object> tourToMap(Tour tour) {
        Map<String, Object> map = new HashMap<>();
        
        if (tour.getTitle() != null) {
            map.put("nombre", tour.getTitle());
        }
        if (tour.getDesc() != null) {
            map.put("descripcion", tour.getDesc());
        }
        if (tour.getLocation() != null) {
            map.put("ubicacion", tour.getLocation());
        }
        if (tour.getDate() != null) {
            map.put("inicioTour", tour.getDate());
        }
        if (tour.getPrice() != null) {
            map.put("costo", tour.getPrice());
        }
        if (tour.getStatus() != null) {
            map.put("estado", tour.getStatus());
        }
        if (tour.getStartTime() != null) {
            map.put("horaInicio", tour.getStartTime());
        }
        if (tour.getEndTime() != null) {
            map.put("horaFin", tour.getEndTime());
        }
        if (tour.getIdiomas() != null) {
            map.put("idiomas", tour.getIdiomas());
        }
        
        // Convertir servicios extras
        if (tour.getServiciosExtras() != null && !tour.getServiciosExtras().isEmpty()) {
            List<Map<String, Object>> serviciosList = new java.util.ArrayList<>();
            for (Tour.ServicioExtra servicio : tour.getServiciosExtras()) {
                Map<String, Object> servicioMap = new HashMap<>();
                servicioMap.put("nombre", servicio.getNombre());
                servicioMap.put("precio", servicio.getPrecio());
                servicioMap.put("descripcion", servicio.getDescripcion());
                serviciosList.add(servicioMap);
            }
            map.put("serviciosExtras", serviciosList);
        }
        
        // Convertir ruta/ubicaciones
        if (tour.getRuta() != null && !tour.getRuta().isEmpty()) {
            List<Map<String, Object>> rutaList = new java.util.ArrayList<>();
            for (Tour.Ubicacion ubicacion : tour.getRuta()) {
                Map<String, Object> ubicacionMap = new HashMap<>();
                ubicacionMap.put("nombre", ubicacion.getNombre());
                ubicacionMap.put("actividades", ubicacion.getActividades());
                rutaList.add(ubicacionMap);
            }
            map.put("ruta", rutaList);
        }
        
        return map;
    }
    
    // ==================== INTERFACES DE CALLBACKS ====================
    
    public interface OnToursLoadedListener {
        void onToursLoaded(List<Tour> tours);
    }
    
    public interface OnTourLoadedListener {
        void onTourLoaded(Tour tour);
    }
    
    public interface OnTourCreatedListener {
        void onTourCreated(boolean success, String tourId);
    }
    
    public interface OnTourUpdatedListener {
        void onTourUpdated(boolean success);
    }
    
    public interface OnTourDeletedListener {
        void onTourDeleted(boolean success);
    }
    
    public interface OnGuidesLoadedListener {
        void onGuidesLoaded(List<Usuario> guides);
    }
    
    public interface OnGuideLoadedListener {
        void onGuideLoaded(Usuario guide);
    }
    
    public interface OnAdminProfileLoadedListener {
        void onAdminProfileLoaded(Usuario admin);
    }
    
    public interface OnAdminProfileUpdatedListener {
        void onAdminProfileUpdated(boolean success);
    }
    
    public interface OnEmpresaLoadedListener {
        void onEmpresaLoaded(String nombreEmpresa);
    }
}
