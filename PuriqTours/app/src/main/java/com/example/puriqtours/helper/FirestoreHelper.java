package com.example.puriqtours.helper;

import android.util.Log;

import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.entity.Reserva;
import com.example.puriqtours.entity.Solicitud;
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
    private static final String COLLECTION_RESERVAS = "reservas";
    private static final String COLLECTION_SOLICITUDES = "solicitudes";
    
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
                    
                    // Cargar subcolección de servicios extra
                    loadExtraServicesSubcollection(tourId, tour, () -> {
                        // Cargar subcolección de ubicaciones
                        loadLocationsSubcollection(tourId, tour, () -> {
                            listener.onTourLoaded(tour);
                        });
                    });
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
     * Cargar subcolección de servicios extra
     */
    private void loadExtraServicesSubcollection(String tourId, Tour tour, Runnable onComplete) {
        db.collection(COLLECTION_TOURS)
            .document(tourId)
            .collection("extraService")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Tour.ServicioExtra> servicios = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Tour.ServicioExtra servicio = new Tour.ServicioExtra();
                    servicio.setTitle(doc.getString("title"));
                    servicio.setImageUrl(doc.getString("imageUrl"));
                    
                    // Leer price como Object y convertir a Float
                    Object priceObj = doc.get("price");
                    if (priceObj != null) {
                        if (priceObj instanceof Number) {
                            servicio.setPrice(((Number) priceObj).floatValue());
                        } else if (priceObj instanceof String) {
                            try {
                                servicio.setPrice(Float.parseFloat((String) priceObj));
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error al convertir precio: " + priceObj, e);
                                servicio.setPrice(0f);
                            }
                        } else {
                            servicio.setPrice(0f);
                        }
                    } else {
                        servicio.setPrice(0f);
                    }
                    
                    servicios.add(servicio);
                }
                tour.setServiciosExtras(servicios);
                Log.d(TAG, "Servicios extra cargados: " + servicios.size());
                onComplete.run();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar servicios extra", e);
                onComplete.run();
            });
    }
    
    /**
     * Cargar subcolección de ubicaciones
     */
    private void loadLocationsSubcollection(String tourId, Tour tour, Runnable onComplete) {
        db.collection(COLLECTION_TOURS)
            .document(tourId)
            .collection("locations")
            .orderBy("order")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Tour.Ubicacion> ubicaciones = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Tour.Ubicacion ubicacion = new Tour.Ubicacion();
                    ubicacion.setTitle(doc.getString("title"));
                    Object orderObj = doc.get("order");
                    if (orderObj instanceof Long) {
                        ubicacion.setOrder(((Long) orderObj).intValue());
                    }
                    Object latObj = doc.get("lat");
                    if (latObj instanceof Double) {
                        ubicacion.setLat((Double) latObj);
                    }
                    Object lngObj = doc.get("lng");
                    if (lngObj instanceof Double) {
                        ubicacion.setLng((Double) lngObj);
                    }
                    ubicaciones.add(ubicacion);
                }
                tour.setRuta(ubicaciones);
                Log.d(TAG, "Ubicaciones cargadas: " + ubicaciones.size());
                onComplete.run();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar ubicaciones", e);
                onComplete.run();
            });
    }
    
    /**
     * Crear un nuevo tour en Firestore con subcollections
     */
    public void createTour(Tour tour, OnTourCreatedListener listener) {
        Map<String, Object> tourData = tourToMap(tour);
        
        db.collection(COLLECTION_TOURS)
            .add(tourData)
            .addOnSuccessListener(documentReference -> {
                String tourId = documentReference.getId();
                tour.setIdTour(tourId);
                Log.d(TAG, "Tour creado con ID: " + tourId);
                
                // Guardar solo subcollection de extraServices
                // locations se guarda manualmente desde CreateTourActivity
                saveExtraServicesSubcollection(tourId, tour.getServiciosExtras());
                
                listener.onTourCreated(true, tourId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al crear tour", e);
                listener.onTourCreated(false, null);
            });
    }
    
    /**
     * Guardar subcollection de locations (ruta) para un tour
     */
    public void saveLocationsSubcollection(String tourId, List<Tour.Ubicacion> ubicaciones) {
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            return;
        }
        
        for (Tour.Ubicacion ubicacion : ubicaciones) {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("title", ubicacion.getTitle());
            locationData.put("order", ubicacion.getOrder());
            locationData.put("lat", ubicacion.getLat());
            locationData.put("lng", ubicacion.getLng());
            
            db.collection(COLLECTION_TOURS)
                .document(tourId)
                .collection("locations")
                .add(locationData)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Location guardada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar location", e));
        }
    }
    
    /**
     * Cargar ubicaciones de un tour específico
     */
    public void loadLocationsByTourId(String tourId, OnLocationsLoadedListener listener) {
        db.collection(COLLECTION_TOURS)
            .document(tourId)
            .collection("locations")
            .orderBy("order")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Tour.Ubicacion> ubicaciones = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Tour.Ubicacion ubicacion = new Tour.Ubicacion();
                    ubicacion.setTitle(doc.getString("title"));
                    Object orderObj = doc.get("order");
                    if (orderObj instanceof Long) {
                        ubicacion.setOrder(((Long) orderObj).intValue());
                    }
                    Object latObj = doc.get("lat");
                    if (latObj instanceof Double) {
                        ubicacion.setLat((Double) latObj);
                    }
                    Object lngObj = doc.get("lng");
                    if (lngObj instanceof Double) {
                        ubicacion.setLng((Double) lngObj);
                    }
                    ubicaciones.add(ubicacion);
                }
                Log.d(TAG, "Ubicaciones cargadas: " + ubicaciones.size());
                listener.onLocationsLoaded(ubicaciones);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar ubicaciones", e);
                listener.onLocationsLoaded(new ArrayList<>());
            });
    }
    
    /**
     * Guardar subcollection de extraServices para un tour
     */
    private void saveExtraServicesSubcollection(String tourId, List<Tour.ServicioExtra> servicios) {
        if (servicios == null || servicios.isEmpty()) {
            return;
        }
        
        for (Tour.ServicioExtra servicio : servicios) {
            Map<String, Object> serviceData = new HashMap<>();
            serviceData.put("title", servicio.getTitle());
            serviceData.put("imageUrl", servicio.getImageUrl());
            serviceData.put("price", servicio.getPrice());
            
            db.collection(COLLECTION_TOURS)
                .document(tourId)
                .collection("extraService")
                .add(serviceData)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Servicio extra guardado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar servicio extra", e));
        }
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
    
    /**
     * Cargar tours creados por una empresa específica
     */
    public void loadToursByEmpresa(String idEmpresa, OnToursLoadedListener listener) {
        db.collection(COLLECTION_TOURS)
            .whereEqualTo("idEmpresa", idEmpresa)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Tour> tours = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Tour tour = documentToTour(document);
                    if (tour != null) {
                        tours.add(tour);
                    }
                }
                Log.d(TAG, "Tours de empresa " + idEmpresa + " cargados: " + tours.size());
                listener.onToursLoaded(tours);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar tours de empresa", e);
                listener.onToursLoaded(new ArrayList<>());
            });
    }
    
    // ==================== RESERVAS ====================
    
    /**
     * Cargar reservas de tours de una empresa específica
     * (Reservas de los tours que creó el admin/empresa)
     */
    public void loadReservasByEmpresa(String idEmpresa, OnReservasLoadedListener listener) {
        // Primero obtenemos los IDs de los tours de la empresa
        db.collection(COLLECTION_TOURS)
            .whereEqualTo("idEmpresa", idEmpresa)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> tourIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    tourIds.add(document.getId());
                }
                
                if (tourIds.isEmpty()) {
                    listener.onReservasLoaded(new ArrayList<>());
                    return;
                }
                
                // Cargar reservas de esos tours
                db.collection(COLLECTION_RESERVAS)
                    .whereIn("idTour", tourIds)
                    .get()
                    .addOnSuccessListener(reservasSnapshot -> {
                        List<Reserva> reservas = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : reservasSnapshot) {
                            Reserva reserva = documentToReserva(doc);
                            if (reserva != null) {
                                reservas.add(reserva);
                            }
                        }
                        Log.d(TAG, "Reservas cargadas: " + reservas.size());
                        listener.onReservasLoaded(reservas);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al cargar reservas", e);
                        listener.onReservasLoaded(new ArrayList<>());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar tours de empresa", e);
                listener.onReservasLoaded(new ArrayList<>());
            });
    }
    
    /**
     * Cargar tours sin guía asignado (idGuia vacío o null)
     * Estos son tours disponibles para asignar un guía
     */
    public void loadToursWithoutGuide(OnToursLoadedListener listener) {
        db.collection(COLLECTION_TOURS)
            .whereEqualTo("idGuia", "")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Tour> tours = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Tour tour = documentToTour(document);
                    if (tour != null) {
                        tours.add(tour);
                    }
                }
                
                // También buscar tours donde idGuia sea null
                db.collection(COLLECTION_TOURS)
                    .whereEqualTo("idGuia", null)
                    .get()
                    .addOnSuccessListener(nullGuideSnapshots -> {
                        for (QueryDocumentSnapshot document : nullGuideSnapshots) {
                            Tour tour = documentToTour(document);
                            if (tour != null && !tours.contains(tour)) {
                                tours.add(tour);
                            }
                        }
                        Log.d(TAG, "Tours sin guía cargados: " + tours.size());
                        listener.onToursLoaded(tours);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error buscando tours con idGuia null, usando solo los vacíos", e);
                        listener.onToursLoaded(tours);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar tours sin guía", e);
                listener.onToursLoaded(new ArrayList<>());
            });
    }
    
    /**
     * Cargar reservas sin guía asignado (solicitudes para guías)
     * Estas son reservas con idGuia vacío o null
     */
    public void loadReservasWithoutGuide(OnReservasLoadedListener listener) {
        db.collection(COLLECTION_RESERVAS)
            .whereEqualTo("idGuia", "")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> reservas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Reserva reserva = documentToReserva(document);
                    if (reserva != null) {
                        reservas.add(reserva);
                    }
                }
                
                // También buscar reservas donde idGuia sea null
                db.collection(COLLECTION_RESERVAS)
                    .whereEqualTo("idGuia", null)
                    .get()
                    .addOnSuccessListener(nullGuideSnapshots -> {
                        for (QueryDocumentSnapshot document : nullGuideSnapshots) {
                            Reserva reserva = documentToReserva(document);
                            if (reserva != null && !reservas.contains(reserva)) {
                                reservas.add(reserva);
                            }
                        }
                        Log.d(TAG, "Reservas sin guía cargadas: " + reservas.size());
                        listener.onReservasLoaded(reservas);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error buscando reservas con idGuia null, usando solo las vacías", e);
                        listener.onReservasLoaded(reservas);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar reservas sin guía", e);
                listener.onReservasLoaded(new ArrayList<>());
            });
    }
    
    /**
     * Asignar un guía a una reserva
     */
    public void assignGuideToReserva(String reservaId, String guideId, OnSuccessListener listener) {
        db.collection(COLLECTION_RESERVAS)
            .document(reservaId)
            .update("idGuia", guideId)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Guía asignado a reserva exitosamente");
                listener.onSuccess(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al asignar guía a reserva", e);
                listener.onSuccess(false);
            });
    }
    
    /**
     * Cargar una reserva específica por ID
     */
    public void loadReservaById(String reservaId, OnReservaLoadedListener listener) {
        db.collection(COLLECTION_RESERVAS)
            .document(reservaId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Reserva reserva = documentToReserva(documentSnapshot);
                    listener.onReservaLoaded(reserva);
                } else {
                    listener.onReservaLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar reserva", e);
                listener.onReservaLoaded(null);
            });
    }
    
    /**
     * Convertir DocumentSnapshot a Reserva
     */
    private Reserva documentToReserva(DocumentSnapshot document) {
        try {
            Reserva reserva = new Reserva();
            reserva.setIdReserva(document.getId());
            
            if (document.contains("idTour")) {
                reserva.setIdTour(document.getString("idTour"));
            }
            if (document.contains("idCliente")) {
                reserva.setIdCliente(document.getString("idCliente"));
            }
            if (document.contains("idGuia")) {
                reserva.setIdGuia(document.getString("idGuia"));
            }
            if (document.contains("title")) {
                reserva.setTitle(document.getString("title"));
            }
            if (document.contains("status")) {
                reserva.setStatus(document.getString("status"));
            }
            if (document.contains("hour")) {
                reserva.setHour(document.getString("hour"));
            }
            if (document.contains("date")) {
                reserva.setDate(document.getString("date"));
            }
            if (document.contains("price")) {
                Object priceObj = document.get("price");
                if (priceObj instanceof Number) {
                    reserva.setPrice(((Number) priceObj).floatValue());
                }
            }
            if (document.contains("qrStart")) {
                reserva.setQrStart(document.getString("qrStart"));
            }
            if (document.contains("qrEnd")) {
                reserva.setQrEnd(document.getString("qrEnd"));
            }
            if (document.contains("travelers")) {
                reserva.setTravelers(document.getString("travelers"));
            }
            if (document.contains("metodoPago")) {
                reserva.setMetodoPago(document.getString("metodoPago"));
            }
            if (document.contains("codigoOperacion")) {
                reserva.setCodigoOperacion(document.getString("codigoOperacion"));
            }
            
            return reserva;
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir documento a Reserva", e);
            return null;
        }
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
            
            // Mapear campos según la nueva estructura de BD
            if (document.contains("title")) {
                tour.setTitle(document.getString("title"));
            }
            if (document.contains("desc")) {
                tour.setDesc(document.getString("desc"));
            }
            if (document.contains("location")) {
                tour.setLocation(document.getString("location"));
            }
            if (document.contains("region")) {
                tour.setRegion(document.getString("region"));
            }
            if (document.contains("price")) {
                Object priceObj = document.get("price");
                if (priceObj instanceof Number) {
                    tour.setPrice(((Number) priceObj).floatValue());
                }
            }
            if (document.contains("status")) {
                tour.setStatus(document.getString("status"));
            }
            if (document.contains("startTime")) {
                tour.setStartTime(document.getString("startTime"));
            }
            if (document.contains("endTime")) {
                tour.setEndTime(document.getString("endTime"));
            }
            if (document.contains("idEmpresa")) {
                tour.setIdEmpresa(document.getString("idEmpresa"));
            }
            if (document.contains("imageUrl")) {
                tour.setImageUrl(document.getString("imageUrl"));
            }
            if (document.contains("rating")) {
                Object ratingObj = document.get("rating");
                if (ratingObj instanceof Number) {
                    tour.setRating(((Number) ratingObj).intValue());
                }
            }
            if (document.contains("idiomas")) {
                tour.setIdiomas(document.getString("idiomas"));
            }
            
            return tour;
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir documento a Tour", e);
            return null;
        }
    }
    
    /**
     * Convertir Tour a Map para Firestore (sin subcollections)
     */
    private Map<String, Object> tourToMap(Tour tour) {
        Map<String, Object> map = new HashMap<>();
        
        if (tour.getTitle() != null) {
            map.put("title", tour.getTitle());
        }
        if (tour.getDesc() != null) {
            map.put("desc", tour.getDesc());
        }
        if (tour.getLocation() != null) {
            map.put("location", tour.getLocation());
        }
        if (tour.getRegion() != null) {
            map.put("region", tour.getRegion());
        }
        if (tour.getPrice() != null) {
            map.put("price", tour.getPrice());
        }
        if (tour.getStatus() != null) {
            map.put("status", tour.getStatus());
        }
        if (tour.getStartTime() != null) {
            map.put("startTime", tour.getStartTime());
        }
        if (tour.getEndTime() != null) {
            map.put("endTime", tour.getEndTime());
        }
        if (tour.getIdEmpresa() != null) {
            map.put("idEmpresa", tour.getIdEmpresa());
        }
        if (tour.getIdGuia() != null) {
            map.put("idGuia", tour.getIdGuia());
        }
        if (tour.getImageUrl() != null) {
            map.put("imageUrl", tour.getImageUrl());
        }
        if (tour.getRating() != null) {
            map.put("rating", tour.getRating());
        }
        if (tour.getIdiomas() != null) {
            map.put("idiomas", tour.getIdiomas());
        }
        
        return map;
    }
    
    // ==================== SOLICITUDES ====================
    
    /**
     * Asignar guía a un tour (actualizar idGuia en tour)
     * Se llama cuando el guía acepta la solicitud
     */
    public void assignGuideToTour(String tourId, String guideUid, OnSuccessListener listener) {
        db.collection(COLLECTION_TOURS)
            .document(tourId)
            .update("idGuia", guideUid)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Guía asignado a tour: " + tourId);
                listener.onSuccess(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al asignar guía a tour", e);
                listener.onSuccess(false);
            });
    }
    
    /**
     * Crear una nueva solicitud en Firestore
     * @param solicitud Solicitud a crear con status="Pendiente" por defecto
     * @param listener Callback con resultado de la operación
     */
    public void createSolicitud(Solicitud solicitud, OnSolicitudCreatedListener listener) {
        // Asegurar que el status sea "Pendiente" al crear
        if (solicitud.getStatus() == null || solicitud.getStatus().isEmpty()) {
            solicitud.setStatus("Pendiente");
        }
        
        db.collection(COLLECTION_SOLICITUDES)
            .add(solicitud.toMap())
            .addOnSuccessListener(documentReference -> {
                String solicitudId = documentReference.getId();
                Log.d(TAG, "Solicitud creada con ID: " + solicitudId);
                listener.onSolicitudCreated(true, solicitudId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al crear solicitud", e);
                listener.onSolicitudCreated(false, null);
            });
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
    
    public interface OnReservasLoadedListener {
        void onReservasLoaded(List<Reserva> reservas);
    }
    
    public interface OnReservaLoadedListener {
        void onReservaLoaded(Reserva reserva);
    }
    
    public interface OnSuccessListener {
        void onSuccess(boolean success);
    }
    
    public interface OnSolicitudCreatedListener {
        void onSolicitudCreated(boolean success, String solicitudId);
    }
    
    public interface OnLocationsLoadedListener {
        void onLocationsLoaded(List<Tour.Ubicacion> ubicaciones);
    }
}
