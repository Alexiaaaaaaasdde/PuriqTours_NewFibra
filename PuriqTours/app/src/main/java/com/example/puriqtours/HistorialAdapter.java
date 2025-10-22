package com.example.puriqtours;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<HistorialTour> listaTours;
    private List<HistorialTour> listaToursOriginal; // copia para filtros
    private Context context;

    public HistorialAdapter(List<HistorialTour> listaTours, Context context) {
        this.listaTours = new ArrayList<>(listaTours);
        this.listaToursOriginal = new ArrayList<>(listaTours);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorialTour tour = listaTours.get(position);

        // 🔹 Pintar datos
        holder.tvTitulo.setText(tour.getTitulo());
        holder.tvUbicacion.setText(tour.getUbicacion());
        holder.tvEstado.setText("Estado: " + tour.getEstado());
        holder.tvDuracion.setText("Duración • " + tour.getDuracion());
        holder.tvPrecio.setText("S/ " + tour.getPrecio());
        holder.ratingBar.setRating(tour.getRating());
        holder.imgTour.setImageResource(tour.getImagenResId());

        // 🔹 Opciones ocultas por defecto
        holder.layoutOpciones.setVisibility(View.GONE);

        // 🔹 Expandir/colapsar opciones al tocar la card
        holder.itemView.setOnClickListener(v -> {
            if (holder.layoutOpciones.getVisibility() == View.VISIBLE) {
                holder.layoutOpciones.setVisibility(View.GONE);
            } else {
                holder.layoutOpciones.setVisibility(View.VISIBLE);
            }
        });

        // 🔹 Botón Detalles
        holder.btnDetalles.setOnClickListener(v -> {
            Intent intent = null;

            if (tour.getEstado().equalsIgnoreCase("En proceso")) {
                intent = new Intent(context, EnProcesoActivity.class);
            } else if (tour.getEstado().equalsIgnoreCase("Reservado")) {
                intent = new Intent(context, ReservadoActivity.class);
            } else if (tour.getEstado().equalsIgnoreCase("Finalizado")) {
                intent = new Intent(context, FinalizadoActivity.class);
            }

            if (intent != null) {
                intent.putExtra("titulo", tour.getTitulo());
                intent.putExtra("ubicacion", tour.getUbicacion());
                intent.putExtra("precio", tour.getPrecio());
                intent.putExtra("duracion", tour.getDuracion());
                intent.putExtra("estado", tour.getEstado());
                intent.putExtra("rating", tour.getRating());
                intent.putExtra("imagen", tour.getImagenResId());

                context.startActivity(intent);
            }
        });


        // 🔹 Botón Chat (solo si está reservado)
        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("estado", tour.getEstado()); // 👈 pasamos el estado
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaTours.size();
    }

    // 🔹 Filtrado por texto (SearchBar)
    public void filtrar(String texto) {
        List<HistorialTour> listaFiltrada = new ArrayList<>();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaToursOriginal);
        } else {
            for (HistorialTour tour : listaToursOriginal) {
                if (tour.getTitulo().toLowerCase().contains(texto.toLowerCase()) ||
                        tour.getUbicacion().toLowerCase().contains(texto.toLowerCase())) {
                    listaFiltrada.add(tour);
                }
            }
        }
        listaTours.clear();
        listaTours.addAll(listaFiltrada);
        notifyDataSetChanged();
    }

    // 🔹 Filtrado por estado (Chips)
    public void filtrarEstado(String estado) {
        List<HistorialTour> listaFiltrada = new ArrayList<>();
        if (estado.isEmpty()) {
            listaFiltrada.addAll(listaToursOriginal);
        } else {
            for (HistorialTour tour : listaToursOriginal) {
                if (tour.getEstado().equalsIgnoreCase(estado)) {
                    listaFiltrada.add(tour);
                }
            }
        }
        listaTours.clear();
        listaTours.addAll(listaFiltrada);
        notifyDataSetChanged();
    }

    // 🔹 ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvUbicacion, tvEstado, tvDuracion, tvPrecio;
        RatingBar ratingBar;
        ImageView imgTour;
        LinearLayout layoutOpciones;
        Button btnChat, btnDetalles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            imgTour = itemView.findViewById(R.id.imgTour);
            layoutOpciones = itemView.findViewById(R.id.layoutOpciones);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
        }
    }

}
