package com.example.puriqtours.cliente;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class DetalleTourActivity extends BaseActivity {

    // ===============================
    // VARIABLES GLOBALES
    // ===============================

    private TextView tvTitulo, tvPrecio, tvFecha, tvViajeros, tvSeleccion, tvDesc;
    private ImageView imgTour, btnCalendario;

    // Cantidades de viajeros
    private int adultos = 2;
    private int ninos = 0;
    private int bebes = 0;

    // Precios
    private float precioAdulto;
    private float precioNino;

    private Dialog dialogDisponibilidad;
    private String fechaSeleccionadaGlobal = "Martes, 15 de Marzo de 2025";
    private String tourId;

    private String tituloTour;
    private float precioTour;
    private String location;
    private int precioExtras = 0;

    private int cantDesayuno = 0;
    private int cantCanotaje = 0;

    private List<Tour.ServicioExtra> extrasDisponibles = new ArrayList<>();
    private Map<String, Integer> cantidadesExtras = new HashMap<>();


    // 🔹 HORARIOS DEL TOUR
    private List<String> horariosDisponibles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_tour);
        setupSharedToolbar();

        tourId = getIntent().getStringExtra("tourId");

        // Referencias UI
        tvTitulo = findViewById(R.id.tvTitulo);
        tvPrecio = findViewById(R.id.tvPreciokuelap);
        tvFecha = findViewById(R.id.tvFecha);
        tvViajeros = findViewById(R.id.tvViajeros);
        tvSeleccion = findViewById(R.id.tvSeleccion);
        tvDesc = findViewById(R.id.tvDescripcionTour);
        imgTour = findViewById(R.id.imgTour);
        btnCalendario = findViewById(R.id.btnCalendario);

        ImageView imgEmpresaLogo = findViewById(R.id.imgEmpresaLogo);
        TextView tvEmpresaTelefono = findViewById(R.id.tvEmpresaTelefono);
        TextView tvEmpresaEmail = findViewById(R.id.tvEmpresaEmail);
        TextView tvEmpresaRuc = findViewById(R.id.tvEmpresaRuc);


        RatingBar ratingBar = findViewById(R.id.ratingBar1);
        Button btnDisponibilidad = findViewById(R.id.btnDisponibilidad);

        // Recuperar datos del tour
        tituloTour = getIntent().getStringExtra("titulo");
        String precioStr = getIntent().getStringExtra("precio");

        try {
            precioTour = Float.parseFloat(precioStr);
        } catch (Exception e) {
            precioTour = 0;
        }

        precioAdulto = precioTour;
        precioNino = precioTour * 0.80f;

        String desc = getIntent().getStringExtra("desc");

        // 🔥 MOSTRAR EMPRESA DEL TOUR
        String idEmpresa = getIntent().getStringExtra("idEmpresa");
        TextView tvEmpresa = findViewById(R.id.tvEmpresa);

        if (idEmpresa != null && !idEmpresa.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("empresas")
                    .document(idEmpresa)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nombreEmpresa = doc.getString("name");
                            tvEmpresa.setText("Ofrecido por: " + nombreEmpresa);
                        } else {
                            tvEmpresa.setText("Empresa no disponible");
                        }
                    })
                    .addOnFailureListener(e -> tvEmpresa.setText("Empresa no disponible"));
        } else {
            tvEmpresa.setText("Empresa no asignada");
        }

        if (idEmpresa != null && !idEmpresa.isEmpty()) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("empresas")
                    .document(idEmpresa)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {

                            String phone = doc.getString("phone");
                            String email = doc.getString("email");
                            String ruc = doc.getString("ruc");
                            String logoUrl = doc.getString("imageUrl");

                            tvEmpresaTelefono.setText("Teléfono: " + phone);
                            tvEmpresaEmail.setText("Email: " + email);
                            tvEmpresaRuc.setText("RUC: " + ruc);

                            if (logoUrl != null && !logoUrl.isEmpty()) {
                                imgEmpresaLogo.setVisibility(View.VISIBLE);
                                Glide.with(this)
                                        .load(logoUrl)
                                        .placeholder(R.drawable.kuelap)
                                        .into(imgEmpresaLogo);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvEmpresaTelefono.setText("Teléfono: No disponible");
                        tvEmpresaEmail.setText("Email: No disponible");
                        tvEmpresaRuc.setText("RUC: No disponible");
                    });
        }



        String img = getIntent().getStringExtra("img");
        location = getIntent().getStringExtra("location");
        int rating = getIntent().getIntExtra("rating", 5);

        tvTitulo.setText(tituloTour);
        tvPrecio.setText("Desde S/ " + String.format("%.2f", precioAdulto));
        tvDesc.setText(desc);
        ratingBar.setRating(rating);

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.kuelap)
                .into(imgTour);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // 🔹 CARGAR HORARIOS DESDE FIRESTORE
        cargarHorariosDesdeFirestore();

        tvViajeros.setOnClickListener(v -> mostrarDialogoViajeros(tvViajeros));
        btnDisponibilidad.setOnClickListener(v -> mostrarDialogoDisponibilidad());

        btnCalendario.setOnClickListener(v -> mostrarDatePicker());
        tvFecha.setOnClickListener(v -> mostrarDatePicker());

        // Bottom Navigation
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .document(tourId)
                .get()
                .addOnSuccessListener(doc -> {
                    Tour tour = doc.toObject(Tour.class);
                    if (tour != null && tour.getServiciosExtras() != null) {
                        extrasDisponibles = tour.getServiciosExtras();
                    }
                });


        // 🔥 CARGAR VALORACIONES DESDE FIREBASE
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

        // ⭐ BOTÓN DETALLES dentro del popup de disponibilidad
        Button btnDetalle = dialogDisponibilidad.findViewById(R.id.btnDetalles);
        if (btnDetalle != null) {
            btnDetalle.setOnClickListener(v -> mostrarDialogoDetalles());
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
            i.putExtra("precio", "S/ " + String.format("%.2f", totalFinal));
            i.putExtra("hora", horaSeleccionada[0]);
            i.putExtra("tourId", tourId);
            i.putExtra("titulo", tituloTour);
            i.putExtra("ubicacion", location);
            i.putExtra("img", getIntent().getStringExtra("img"));

            // 🔹 EXTRA: enviar detalles de los extras
            String extrasDetalle = "";

            if (precioExtras > 0) {
                if (cantDesayuno > 0) extrasDetalle += cantDesayuno + "× Desayuno ";
                if (cantCanotaje > 0) extrasDetalle += cantCanotaje + "× Canotaje ";
            } else {
                extrasDetalle = "Sin extras";
            }


            i.putExtra("extrasDetalle", extrasDetalle);
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
                // ❌ QUITAMOS orderBy PARA EVITAR ERROR CON Timestamps inválidos
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

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_detalles);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // ================
        // 🔹 BOTONES EXTRA
        // ================
        ImageButton btnMasDesayuno = dialog.findViewById(R.id.btnMasDesayuno);
        ImageButton btnMenosDesayuno = dialog.findViewById(R.id.btnMenosDesayuno);
        TextView tvCantidadDesayuno = dialog.findViewById(R.id.tvCantidadDesayuno);

        ImageButton btnMasCanotaje = dialog.findViewById(R.id.btnMasCanotaje);
        ImageButton btnMenosCanotaje = dialog.findViewById(R.id.btnMenosCanotaje);
        TextView tvCantidadCanotaje = dialog.findViewById(R.id.tvCantidadCanotaje);

        Button btnAgregarExtras = dialog.findViewById(R.id.btnAgregarExtras);
        Button btnSalir = dialog.findViewById(R.id.btnSalir);

        // Mostrar cantidades actuales
        tvCantidadDesayuno.setText(String.valueOf(cantDesayuno));
        tvCantidadCanotaje.setText(String.valueOf(cantCanotaje));

        // 🔸 SUMAR
        btnMasDesayuno.setOnClickListener(v -> {
            cantDesayuno++;
            tvCantidadDesayuno.setText(String.valueOf(cantDesayuno));
        });

        btnMasCanotaje.setOnClickListener(v -> {
            cantCanotaje++;
            tvCantidadCanotaje.setText(String.valueOf(cantCanotaje));
        });

        // 🔸 RESTAR
        btnMenosDesayuno.setOnClickListener(v -> {
            if (cantDesayuno > 0) cantDesayuno--;
            tvCantidadDesayuno.setText(String.valueOf(cantDesayuno));
        });

        btnMenosCanotaje.setOnClickListener(v -> {
            if (cantCanotaje > 0) cantCanotaje--;
            tvCantidadCanotaje.setText(String.valueOf(cantCanotaje));
        });

        // 🔹 GUARDAR EXTRAS
        btnAgregarExtras.setOnClickListener(v -> {

            precioExtras = (cantDesayuno * 10) + (cantCanotaje * 30);

            Toast.makeText(
                    this,
                    "Extras añadidos: S/ " + precioExtras,
                    Toast.LENGTH_SHORT
            ).show();

            dialog.dismiss();

            actualizarPrecioConExtras();
        });

        btnSalir.setOnClickListener(v -> dialog.dismiss());

        dialog.show();


    // =============================
        // 🔹 BOTÓN SALIR
        // =============================
        btnSalir.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


}
