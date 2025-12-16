package com.example.puriqtours.cliente;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.adapter.ServicioExtraAdapter;
import com.example.puriqtours.entity.Tour;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.graphics.Color;

public class DetalleTourActivity extends BaseActivity {

    // ===============================
    // VARIABLES GLOBALES
    // ===============================

    private TextView tvTitulo, tvPrecio, tvFecha, tvViajeros, tvSeleccion, tvDesc;
    private ImageView imgTour, btnCalendario;
    private TextView tvEmpresa;

    // Cantidades de viajeros
    private int adultos = 2;
    private int ninos = 0;
    private int bebes = 0;

    // Precios
    private float precioAdulto;
    private float precioNino;

    private Dialog dialogDisponibilidad;
    private String fechaSeleccionadaGlobal = "Martes, 15 de Marzo de 2025";
    private String tourId, idGuia;

    private String tituloTour;
    private float precioTour;
    private String location;
    private int precioExtras = 0;

    private List<Tour.ServicioExtra> extrasDisponibles = new ArrayList<>();
    private Map<String, Integer> cantidadesExtras = new HashMap<>();
    private boolean extrasYaCargados = false;

    // 🔹 HORARIOS DEL TOUR
    private List<String> horariosDisponibles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_tour);
        setupSharedToolbar();

        // ===============================
        // 🔹 REFERENCIAS UI (SIEMPRE PRIMERO)
        // ===============================
        tvTitulo = findViewById(R.id.tvTitulo);
        tvPrecio = findViewById(R.id.tvPreciokuelap);
        tvFecha = findViewById(R.id.tvFecha);
        tvViajeros = findViewById(R.id.tvViajeros);
        tvSeleccion = findViewById(R.id.tvSeleccion);
        tvDesc = findViewById(R.id.tvDescripcionTour);
        imgTour = findViewById(R.id.imgTour);
        btnCalendario = findViewById(R.id.btnCalendario);
        tvEmpresa = findViewById(R.id.tvEmpresa);

        ImageView imgEmpresaLogo = findViewById(R.id.imgEmpresaLogo);
        TextView tvEmpresaTelefono = findViewById(R.id.tvEmpresaTelefono);
        TextView tvEmpresaEmail = findViewById(R.id.tvEmpresaEmail);

        RatingBar ratingBar = findViewById(R.id.ratingBar1);
        Button btnDisponibilidad = findViewById(R.id.btnDisponibilidad);

        // ===============================
        // 🔹 DATOS DESDE INTENT
        // ===============================
        tourId = getIntent().getStringExtra("tourId");
        idGuia = getIntent().getStringExtra("idGuia");
        tituloTour = getIntent().getStringExtra("titulo");
        location = getIntent().getStringExtra("location");

        String img = getIntent().getStringExtra("img");
        String precioStr = getIntent().getStringExtra("precio");
        String desc = getIntent().getStringExtra("desc");
        int rating = getIntent().getIntExtra("rating", 5);

        // ===============================
        // 🔹 PRECIO
        // ===============================
        try {
            precioTour = Float.parseFloat(precioStr);
        } catch (Exception e) {
            precioTour = 0;
        }

        precioAdulto = precioTour;
        precioNino = precioTour * 0.80f;

        // ===============================
        // 🔹 MOSTRAR DATOS DEL TOUR
        // ===============================
        tvTitulo.setText(tituloTour);
        tvPrecio.setText("Desde S/ " + String.format("%.2f", precioAdulto));
        tvDesc.setText(desc);
        ratingBar.setRating(rating);

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.kuelap)
                .into(imgTour);

        // ===============================
        // 🔹 MOSTRAR EMPRESA (CORRECTO)
        // ===============================
        String idEmpresa = getIntent().getStringExtra("idEmpresa");

        if (idEmpresa != null && !idEmpresa.isEmpty()) {
            cargarEmpresaDesdeUsuario(idEmpresa);
        } else {
            tvEmpresa.setText("Empresa no asignada");
        }

        // ===============================
        // 🔹 BOTONES Y LISTENERS
        // ===============================
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        cargarHorariosDesdeFirestore();

        tvViajeros.setOnClickListener(v -> mostrarDialogoViajeros(tvViajeros));
        btnDisponibilidad.setOnClickListener(v -> mostrarDialogoDisponibilidad());

        btnCalendario.setOnClickListener(v -> mostrarDatePicker());
        tvFecha.setOnClickListener(v -> mostrarDatePicker());

        // ===============================
        // 🔹 BOTTOM NAVIGATION
        // ===============================
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_tours);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                return true;
            }
            return false;
        });

        // ===============================
        // 🔹 VALORACIONES
        // ===============================
        cargarValoracionesDesdeFirebase();
    }


    // 🔹 CARGAR HORARIOS DESDE FIRESTORE
    private void cargarHorariosDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .document(tourId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Intentar obtener horarios desde Firestore
                        List<String> horarios = (List<String>) doc.get("horarios");

                        if (horarios != null && !horarios.isEmpty()) {
                            horariosDisponibles = horarios;
                        } else {
                            // Si no hay horarios en Firestore, usar los del tour
                            String startTime = doc.getString("startTime");
                            String endTime = doc.getString("endTime");

                            if (startTime != null && !startTime.isEmpty()) {
                                horariosDisponibles.add(startTime);
                            }
                            if (endTime != null && !endTime.isEmpty() && !endTime.equals(startTime)) {
                                horariosDisponibles.add(endTime);
                            }

                            // Si aún no hay horarios, usar predeterminados
                            if (horariosDisponibles.isEmpty()) {
                                horariosDisponibles.add("9:00am");
                                horariosDisponibles.add("2:00pm");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // En caso de error, usar horarios predeterminados
                    horariosDisponibles.add("9:00am");
                    horariosDisponibles.add("2:00pm");
                });
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                    fechaSeleccionadaGlobal = sdf.format(sel.getTime());
                    tvFecha.setText(fechaSeleccionadaGlobal);

                    if (dialogDisponibilidad != null && dialogDisponibilidad.isShowing()) {
                        TextView tvFechaSel = dialogDisponibilidad.findViewById(R.id.tvFechaSeleccionada);
                        tvFechaSel.setText(fechaSeleccionadaGlobal);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dp.show();
    }

    private void mostrarDialogoViajeros(TextView tvResumenDestino) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_viajeros);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvAdultos = dialog.findViewById(R.id.tvCantidadAdultos);
        TextView tvNinos = dialog.findViewById(R.id.tvCantidadNinos);
        TextView tvBebes = dialog.findViewById(R.id.tvCantidadBebes);
        TextView tvPrecioAdultoDialog = dialog.findViewById(R.id.tvPrecioAdulto);
        TextView tvPrecioNinoDialog = dialog.findViewById(R.id.tvPrecioNino);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarViajeros);

        tvAdultos.setText("" + adultos);
        tvNinos.setText("" + ninos);
        tvBebes.setText("" + bebes);

        tvPrecioAdultoDialog.setText("S/ " + String.format("%.2f", precioAdulto));
        tvPrecioNinoDialog.setText("S/ " + String.format("%.2f", precioNino));

        dialog.findViewById(R.id.btnMasAdultos).setOnClickListener(v -> {
            adultos++;
            tvAdultos.setText("" + adultos);
        });
        dialog.findViewById(R.id.btnMenosAdultos).setOnClickListener(v -> {
            if (adultos > 1) adultos--;
            tvAdultos.setText("" + adultos);
        });

        dialog.findViewById(R.id.btnMasNinos).setOnClickListener(v -> {
            ninos++;
            tvNinos.setText("" + ninos);
        });
        dialog.findViewById(R.id.btnMenosNinos).setOnClickListener(v -> {
            if (ninos > 0) ninos--;
            tvNinos.setText("" + ninos);
        });

        dialog.findViewById(R.id.btnMasBebes).setOnClickListener(v -> {
            bebes++;
            tvBebes.setText("" + bebes);
        });
        dialog.findViewById(R.id.btnMenosBebes).setOnClickListener(v -> {
            if (bebes > 0) bebes--;
            tvBebes.setText("" + bebes);
        });

        btnAceptar.setOnClickListener(v -> {
            actualizarTextoViajeros(tvResumenDestino);

            if (dialogDisponibilidad != null && dialogDisponibilidad.isShowing()) {
                TextView tvPrecioDisp = dialogDisponibilidad.findViewById(R.id.tvPrecio);
                TextView tvViajerosSel = dialogDisponibilidad.findViewById(R.id.tvViajerosSeleccionados);

                float nuevoTotal = calcularTotalReserva();
                tvPrecioDisp.setText("Total: S/ " + String.format("%.2f", nuevoTotal));
                actualizarTextoViajeros(tvViajerosSel);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void actualizarTextoViajeros(TextView tv) {
        String t = adultos + " adultos";
        if (ninos > 0) t += ", " + ninos + " niños";
        if (bebes > 0) t += ", " + bebes + " bebés";
        tv.setText(t);
    }

    // 🔹 DIÁLOGO DE DISPONIBILIDAD CON HORARIOS DINÁMICOS
    private void mostrarDialogoDisponibilidad() {
        dialogDisponibilidad = new Dialog(this);
        dialogDisponibilidad.setContentView(R.layout.dialog_disponibilidad);
        dialogDisponibilidad.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button btnDetalle = dialogDisponibilidad.findViewById(R.id.btnDetalles);
        if (btnDetalle != null) {
            btnDetalle.setOnClickListener(v -> {

                btnDetalle.setEnabled(false);
                btnDetalle.setText("Cargando...");

                // Si ya están cargados, abrir directo
                if (extrasYaCargados && !extrasDisponibles.isEmpty()) {
                    btnDetalle.setEnabled(true);
                    btnDetalle.setText("Detalles");
                    mostrarDialogoDetalles();
                } else {
                    // Si no, cargar primero
                    cargarExtrasDesdeFirestore(() -> {
                        btnDetalle.setEnabled(true);
                        btnDetalle.setText("Detalles");
                        mostrarDialogoDetalles();
                    });
                }
            });
        }


        TextView tvTituloPopup = dialogDisponibilidad.findViewById(R.id.tvTituloPopup);
        TextView tvFechaSel = dialogDisponibilidad.findViewById(R.id.tvFechaSeleccionada);
        TextView tvViajerosSel = dialogDisponibilidad.findViewById(R.id.tvViajerosSeleccionados);
        TextView tvPrecioDisp = dialogDisponibilidad.findViewById(R.id.tvPrecio);
        LinearLayout layoutHorarios = dialogDisponibilidad.findViewById(R.id.layoutHorarios);
        Button btnReserva = dialogDisponibilidad.findViewById(R.id.btnReserva);

        tvTituloPopup.setText(tituloTour);
        tvFechaSel.setText(fechaSeleccionadaGlobal);
        actualizarTextoViajeros(tvViajerosSel);

        float total = calcularTotalReserva();
        tvPrecioDisp.setText("Total: S/ " + String.format("%.2f", total));

        tvViajerosSel.setOnClickListener(v -> mostrarDialogoViajeros(tvViajerosSel));

        // 🔹 CREAR BOTONES DE HORARIOS DINÁMICAMENTE
        layoutHorarios.removeAllViews();
        final String[] horaSeleccionada = {""};
        final List<Button> botonesHorarios = new ArrayList<>();

        // 🔹 DETERMINAR TAMAÑO SEGÚN CANTIDAD DE HORARIOS
        int cantidadHorarios = horariosDisponibles.size();
        int columnas = cantidadHorarios <= 3 ? cantidadHorarios : 3; // Máximo 3 por fila

        LinearLayout filaActual = null;

        for (int i = 0; i < horariosDisponibles.size(); i++) {
            String horario = horariosDisponibles.get(i);

            // Crear nueva fila cada 3 botones
            if (i % columnas == 0) {
                filaActual = new LinearLayout(this);
                filaActual.setOrientation(LinearLayout.HORIZONTAL);
                filaActual.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                layoutHorarios.addView(filaActual);
            }

            Button btnHorario = new Button(this);
            btnHorario.setText(horario);
            btnHorario.setBackgroundTintList(getResources().getColorStateList(R.color.teal_700));
            btnHorario.setTextColor(getResources().getColor(android.R.color.white));
            btnHorario.setAllCaps(false);
            btnHorario.setTextSize(14);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            );
            params.setMargins(4, 4, 4, 4);
            btnHorario.setLayoutParams(params);

            botonesHorarios.add(btnHorario);

            btnHorario.setOnClickListener(v -> {
                // 🎨 DESELECCIONAR TODOS (color original)
                for (Button btn : botonesHorarios) {
                    btn.setBackgroundTintList(getResources().getColorStateList(R.color.teal_200));
                    btn.setTextColor(getResources().getColor(android.R.color.white));
                }

                // 🎨 SELECCIONAR ESTE (color destacado)
                btnHorario.setBackgroundTintList(getResources().getColorStateList(R.color.teal_700));
                btnHorario.setTextColor(getResources().getColor(android.R.color.white));

                horaSeleccionada[0] = horario;

                btnReserva.setEnabled(true);
                btnReserva.setAlpha(1f);
            });

            filaActual.addView(btnHorario);
        }

        // Inicialmente deshabilitado
        btnReserva.setEnabled(false);
        btnReserva.setAlpha(0.5f);

        btnReserva.setOnClickListener(v -> {
            float totalFinal = calcularTotalReserva() + precioExtras;

            Intent i = new Intent(this, PagoActivity.class);
            i.putExtra("fecha", fechaSeleccionadaGlobal);
            i.putExtra("viajeros", tvViajerosSel.getText().toString());
            i.putExtra("precio", totalFinal);
            i.putExtra("hora", horaSeleccionada[0]);
            i.putExtra("tourId", tourId);
            i.putExtra("idGuia", idGuia);
            i.putExtra("titulo", tituloTour);
            i.putExtra("ubicacion", location);
            i.putExtra("img", getIntent().getStringExtra("img"));

            // ✅ CONSTRUIR LISTA DE EXTRAS
            ArrayList<String> extrasDetalle = new ArrayList<>();
            for (Tour.ServicioExtra extra : extrasDisponibles) {
                int cant = cantidadesExtras.getOrDefault(extra.getTitle(), 0);
                if (cant > 0) {
                    extrasDetalle.add(cant + "× " + extra.getTitle());
                }
            }

            if (extrasDetalle.isEmpty()) {
                extrasDetalle.add("Sin extras");
            }

            // ✅ ENVIAR COMO ARRAYLIST (NO COMO STRING)
            i.putStringArrayListExtra("extrasDetalle", extrasDetalle);
            i.putExtra("precioExtras", precioExtras);

            startActivity(i);
        });


        dialogDisponibilidad.show();
    }

    private float calcularTotalReserva() {
        float total = 0;
        total += adultos * precioAdulto;
        total += ninos * precioNino;
        return total;
    }

    // ===============================
    // 🔥 MÉTODOS PARA VALORACIONES
    // ===============================

    /**
     * Carga las valoraciones del tour desde Firebase
     */
    private void cargarValoracionesDesdeFirebase() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("valoraciones")
                .whereEqualTo("tourId", tourId)
                .limit(2)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    LinearLayout contenedor = findViewById(R.id.contenedorOpiniones);
                    View cardEjemplo1 = findViewById(R.id.cardOpinionEjemplo1);
                    View cardEjemplo2 = findViewById(R.id.cardOpinionEjemplo2);

                    // Si no hay valoraciones → dejar tarjetas de ejemplo
                    if (querySnapshot.isEmpty()) {
                        return;
                    }

                    // Sí hay valoraciones → ocultar las tarjetas de ejemplo
                    if (cardEjemplo1 != null) cardEjemplo1.setVisibility(View.GONE);
                    if (cardEjemplo2 != null) cardEjemplo2.setVisibility(View.GONE);

                    double suma = 0;
                    int count = 0;

                    // Mostrar cada valoración real
                    for (DocumentSnapshot doc : querySnapshot) {

                        String clienteId = doc.getString("clienteId");
                        Double rating = doc.getDouble("ratingPromedio");
                        String comentario = doc.getString("comentario");
                        String fecha = doc.getString("fecha");

                        if (rating != null) suma += rating;
                        count++;

                        cargarNombreYCrearTarjeta(clienteId, rating, comentario, fecha, contenedor);
                    }

                    // Actualizar rating visual
                    RatingBar ratingBar = findViewById(R.id.ratingBar1);
                    TextView tvOpiniones = findViewById(R.id.tvOpiniones);

                    float promedio = (float) (suma / count);
                    ratingBar.setRating(promedio);
                    tvOpiniones.setText("(" + count + " opiniones)");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar valoraciones", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Carga el nombre del cliente y crea la tarjeta de opinión
     */
    private void cargarNombreYCrearTarjeta(String clienteId, Double rating, String comentario,
                                           String fecha, LinearLayout contenedor) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")   // ← CORREGIDO
                .document(clienteId)
                .get()
                .addOnSuccessListener(doc -> {
                    String nombreCompleto = "Usuario";

                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String lastName = doc.getString("last_name");

                        if (name != null && !name.isEmpty()) {
                            nombreCompleto = name;
                            if (lastName != null && !lastName.isEmpty()) {
                                nombreCompleto += " " + lastName.charAt(0) + ".";
                            }
                        }
                    }

                    crearTarjetaOpinion(contenedor, nombreCompleto, rating, comentario, fecha);
                })
                .addOnFailureListener(e -> {
                    crearTarjetaOpinion(contenedor, "Usuario", rating, comentario, fecha);
                });
    }


    /**
     * Crea una tarjeta de opinión dinámicamente
     */
    private void crearTarjetaOpinion(LinearLayout contenedor, String nombre, Double rating,
                                     String comentario, String fecha) {
        // Inflar el layout
        View cardView = LayoutInflater.from(this)
                .inflate(R.layout.item_opinion, contenedor, false);

        // Configurar los datos
        TextView tvNombre = cardView.findViewById(R.id.tvNombreOpinion);
        RatingBar ratingBar = cardView.findViewById(R.id.ratingBarOpinion);
        TextView tvFecha = cardView.findViewById(R.id.tvFechaOpinion);
        TextView tvComentario = cardView.findViewById(R.id.tvComentarioOpinion);

        tvNombre.setText(nombre);
        ratingBar.setRating(rating != null ? rating.floatValue() : 0);

        // Formatear fecha (solo la parte de la fecha, no la hora)
        if (fecha != null && fecha.length() >= 10) {
            tvFecha.setText("Escrito el " + fecha.substring(0, 10));
        } else {
            tvFecha.setText("Fecha no disponible");
        }

        // Mostrar comentario o mensaje por defecto
        if (comentario != null && !comentario.trim().isEmpty()) {
            tvComentario.setText(comentario);
        } else {
            tvComentario.setText("Sin comentario adicional");
        }

        // Agregar al contenedor
        contenedor.addView(cardView);
    }

    /**
     * Actualiza el rating global del tour
     */
    private void actualizarRatingGlobal() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("valoraciones")
                .whereEqualTo("tourId", tourId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) return;

                    double sumaRatings = 0;
                    int contador = querySnapshot.size();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Double rating = doc.getDouble("ratingPromedio");
                        if (rating != null) {
                            sumaRatings += rating;
                        }
                    }

                    float promedioFinal = (float) (sumaRatings / contador);

                    // Actualizar el RatingBar y el contador
                    RatingBar ratingBar = findViewById(R.id.ratingBar1);
                    TextView tvOpiniones = findViewById(R.id.tvOpiniones);

                    ratingBar.setRating(promedioFinal);
                    tvOpiniones.setText("(" + contador + " opiniones)");
                })
                .addOnFailureListener(e -> {
                    // Error al cargar, mantener valores por defecto
                });
    }

    private void actualizarPrecioConExtras() {
        if (dialogDisponibilidad == null) return;

        TextView tvPrecioDisp = dialogDisponibilidad.findViewById(R.id.tvPrecio);

        if (tvPrecioDisp != null) {
            float totalBase = calcularTotalReserva();
            float totalConExtras = totalBase + precioExtras;

            tvPrecioDisp.setText("Total: S/ " + String.format("%.2f", totalConExtras));
        }
    }


    private void mostrarDialogoDetalles() {

        Log.d("DEBUG_DETALLES", "=== Abriendo diálogo ===");
        Log.d("DEBUG_DETALLES", "Extras disponibles: " + extrasDisponibles.size());

        if (extrasDisponibles == null || extrasDisponibles.isEmpty()) {
            Toast.makeText(this, "No hay servicios extra disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_detalles, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvHoraInicio = dialogView.findViewById(R.id.tvHoraInicio);
        TextView tvIdiomas = dialogView.findViewById(R.id.tvIdiomas);
        TextView tvFechaTour = dialogView.findViewById(R.id.tvFechaTour);
        RecyclerView rvExtras = dialogView.findViewById(R.id.rvServiciosExtras);
        Button btnAgregarExtras = dialogView.findViewById(R.id.btnAgregarExtras);
        Button btnSalir = dialogView.findViewById(R.id.btnSalir);
        Button btnVerMapa = dialogView.findViewById(R.id.btnVerMapa);
        ImageView imgMapaPreview = dialogView.findViewById(R.id.imgMapaPreview);
        View overlayMapa = dialogView.findViewById(R.id.overlayMapa);
        LinearLayout containerRuta = dialogView.findViewById(R.id.containerRuta);

        String horarioActual = horariosDisponibles.isEmpty() ? "9:00am" : horariosDisponibles.get(0);
        tvHoraInicio.setText(horarioActual);
        tvIdiomas.setText("Español");
        tvFechaTour.setText(fechaSeleccionadaGlobal);

        // ✅ Configurar RecyclerView
        rvExtras.setLayoutManager(new LinearLayoutManager(this));
        rvExtras.setNestedScrollingEnabled(true);

        // CREAR ADAPTER CON LOS DATOS YA CARGADOS
        ServicioExtraAdapter adapter = new ServicioExtraAdapter(extrasDisponibles, cantidadesExtras);
        rvExtras.setAdapter(adapter);

        Log.d("DEBUG_DETALLES", "Adapter creado con " + adapter.getItemCount() + " items");

        // 🗺️ Cargar mapa y ruta
        cargarRutaYVistaPrevia(imgMapaPreview, containerRuta);
        overlayMapa.setOnClickListener(v -> abrirMapaCompleto());
        btnVerMapa.setOnClickListener(v -> abrirMapaCompleto());

        btnAgregarExtras.setOnClickListener(v -> {
            cantidadesExtras.clear();
            cantidadesExtras.putAll(adapter.getCantidadesSeleccionadas());

            precioExtras = Math.round(adapter.getPrecioTotalExtras());

            Log.d("DEBUG_DETALLES", "Extras agregados. Precio: " + precioExtras);

            actualizarPrecioConExtras();
            dialog.dismiss();
        });

        btnSalir.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // 🗺️ MÉTODO PARA ABRIR EL MAPA COMPLETO

    private void abrirMapaCompleto() {
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID del tour no disponible", Toast.LENGTH_SHORT).show();
            Log.e("DetalleTour", "tourId es null o vacío");
            return;
        }

        Log.d("DetalleTour", "Abriendo mapa para tourId: " + tourId);
        Log.d("DetalleTour", "Título del tour: " + tituloTour);

        Intent intent = new Intent(this, RutaTourActivity.class);
        intent.putExtra("tourId", tourId);
        intent.putExtra("titulo", tituloTour);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e("DetalleTour", "Error al abrir RutaTourActivity", e);
            Toast.makeText(this, "Error al abrir el mapa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 🗺️ CARGAR RUTA Y GENERAR VISTA PREVIA DEL MAPA
    private void cargarRutaYVistaPrevia(ImageView imgMapaPreview, LinearLayout containerRuta) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .document(tourId)
                .collection("locations")
                .orderBy("order")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    containerRuta.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        TextView tvSinRuta = new TextView(this);
                        tvSinRuta.setText("No hay ruta disponible");
                        tvSinRuta.setTextColor(getResources().getColor(R.color.teal_700));
                        containerRuta.addView(tvSinRuta);
                        return;
                    }

                    List<String> coordenadas = new ArrayList<>();

                    int index = 1;
                    for (DocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");

                        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                            coordenadas.add(lat + "," + lng);

                            // 📝 Agregar a la lista de puntos
                            TextView tvPunto = new TextView(this);
                            tvPunto.setText(index + ". " + title);
                            tvPunto.setTextColor(getResources().getColor(R.color.teal_700));
                            tvPunto.setTextSize(14);
                            tvPunto.setPadding(0, 8, 0, 8);
                            containerRuta.addView(tvPunto);

                            index++;
                        }
                    }

                    // 🗺️ GENERAR Y CARGAR IMAGEN DE VISTA PREVIA
                    if (!coordenadas.isEmpty()) {
                        String mapaUrl = generarUrlMapaEstatico(coordenadas);

                        Glide.with(this)
                                .load(mapaUrl)
                                .placeholder(R.drawable.ruta)
                                .error(R.drawable.ruta)
                                .into(imgMapaPreview);
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ruta", Toast.LENGTH_SHORT).show();
                });
    }

    // 🗺️ GENERAR URL DE GOOGLE MAPS STATIC API
    private String generarUrlMapaEstatico(List<String> coordenadas) {

        String apiKey = getString(R.string.google_maps_key);

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");
        url.append("size=600x300");
        url.append("&maptype=roadmap");

        for (int i = 0; i < coordenadas.size(); i++) {
            url.append("&markers=color:red%7Clabel:")
                    .append(i + 1)
                    .append("%7C")
                    .append(coordenadas.get(i));
        }

        url.append("&path=color:0x2B746CFF%7Cweight:5");
        for (String coord : coordenadas) {
            url.append("%7C").append(coord);
        }

        url.append("&key=").append(apiKey);

        return url.toString();
    }


    // 🗺️ CARGAR SOLO LA LISTA DE UBICACIONES
    private void cargarListaRuta(LinearLayout containerRuta) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .document(tourId)
                .collection("locations")
                .orderBy("order")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    containerRuta.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        TextView tvSinRuta = new TextView(this);
                        tvSinRuta.setText("No hay ruta disponible");
                        tvSinRuta.setTextColor(getResources().getColor(R.color.teal_700));
                        containerRuta.addView(tvSinRuta);
                        return;
                    }

                    int index = 1;
                    for (DocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");

                        TextView tvPunto = new TextView(this);
                        tvPunto.setText(index + ". " + title);
                        tvPunto.setTextColor(getResources().getColor(R.color.teal_700));
                        tvPunto.setTextSize(14);
                        tvPunto.setPadding(0, 8, 0, 8);
                        containerRuta.addView(tvPunto);

                        index++;
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ruta", Toast.LENGTH_SHORT).show();
                });
    }

    // 🗺️ MÉTODO PARA CARGAR LA RUTA DESDE FIRESTORE
    private void cargarRutaDelTour(GoogleMap googleMap, LinearLayout containerRuta) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .document(tourId)
                .collection("locations")
                .orderBy("order")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No hay ruta disponible", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<LatLng> rutaPuntos = new ArrayList<>();
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                    // 🗺️ LIMPIAR CONTENEDOR DE RUTA
                    containerRuta.removeAllViews();

                    int index = 1;
                    for (DocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");

                        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                            LatLng punto = new LatLng(lat, lng);
                            rutaPuntos.add(punto);
                            boundsBuilder.include(punto);

                            // 🗺️ AGREGAR MARCADOR
                            googleMap.addMarker(new MarkerOptions()
                                    .position(punto)
                                    .title(index + ". " + title));

                            // 📝 AGREGAR A LA LISTA DE PUNTOS
                            TextView tvPunto = new TextView(this);
                            tvPunto.setText(index + ". " + title);
                            tvPunto.setTextColor(getResources().getColor(R.color.teal_700));
                            tvPunto.setTextSize(14);
                            tvPunto.setPadding(0, 8, 0, 8);
                            containerRuta.addView(tvPunto);

                            index++;
                        }
                    }

                    // 🗺️ DIBUJAR LÍNEA DE LA RUTA
                    if (rutaPuntos.size() > 1) {
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(rutaPuntos)
                                .width(8f)
                                .color(Color.parseColor("#2B746C"))
                                .geodesic(true);

                        googleMap.addPolyline(polylineOptions);

                        // 🗺️ AJUSTAR CÁMARA PARA MOSTRAR TODA LA RUTA
                        LatLngBounds bounds = boundsBuilder.build();
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ruta", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarExtrasDesdeFirestore(Runnable onFinish) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .document(tourId)
                .collection("extraService")
                .get()
                .addOnSuccessListener(query -> {

                    extrasDisponibles.clear(); // 👈 CLAVE

                    for (DocumentSnapshot doc : query) {

                        String title = doc.getString("title");
                        String imageUrl = doc.getString("imageUrl");

                        Double priceDouble = doc.getDouble("price");
                        Float price = priceDouble != null ? priceDouble.floatValue() : 0f;

                        Tour.ServicioExtra extra = new Tour.ServicioExtra();
                        extra.setTitle(title);
                        extra.setImageUrl(imageUrl);
                        extra.setPrice(price);

                        extrasDisponibles.add(extra);
                    }

                    extrasYaCargados = true;

                    if (onFinish != null) onFinish.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar extras", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarEmpresaDesdeUsuario(String idEmpresa) {

        ImageView imgEmpresaLogo = findViewById(R.id.imgEmpresaLogo);
        TextView tvEmpresa = findViewById(R.id.tvEmpresa);
        TextView tvEmpresaTelefono = findViewById(R.id.tvEmpresaTelefono);
        TextView tvEmpresaEmail = findViewById(R.id.tvEmpresaEmail);
        TextView tvEmpresaDireccion = findViewById(R.id.tvEmpresaDireccion);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users") // ✅ USERS, NO EMPRESAS
                .document(idEmpresa)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        tvEmpresa.setText("Empresa no disponible");
                        return;
                    }

                    String rol = doc.getString("rol");
                    if (!"Admin".equals(rol)) {
                        tvEmpresa.setText("Empresa no válida");
                        return;
                    }

                    String nombre = doc.getString("name");
                    String email = doc.getString("email");
                    String phone = doc.getString("phone");
                    String logoUrl = doc.getString("profile_image");

                    // 🔹 NUEVO: dirección
                    String direccion = doc.getString("address");

                    tvEmpresa.setText("Ofrecido por: " + nombre);
                    tvEmpresaTelefono.setText("Teléfono: " + phone);
                    tvEmpresaEmail.setText("Email: " + email);

                    if (direccion != null && !direccion.isEmpty()) {
                        tvEmpresaDireccion.setText("Dirección: " + direccion);
                    } else {
                        tvEmpresaDireccion.setText("Dirección: No disponible");
                    }

                    if (logoUrl != null && !logoUrl.isEmpty()) {
                        imgEmpresaLogo.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(logoUrl)
                                .placeholder(R.drawable.kuelap)
                                .into(imgEmpresaLogo);
                    }
                })

                .addOnFailureListener(e -> {
                    tvEmpresa.setText("Empresa no disponible");
                });
    }



}
