package com.example.puriqtours.adapter;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.cliente.ChatActivity;
import com.example.puriqtours.cliente.EnProcesoActivity;
import com.example.puriqtours.cliente.FinalizadoActivity;
import com.example.puriqtours.cliente.ReservadoActivity;
import com.example.puriqtours.entity.HistorialTour;

import java.util.ArrayList;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<HistorialTour> listaTours;
    private List<HistorialTour> listaToursOriginal;
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

        // ---- MOSTRAR DATOS ----
        holder.tvTitulo.setText(tour.getTitulo());
        holder.tvUbicacion.setText(""); // ya no existe, se deja vacío
        holder.tvEstado.setText("Estado: " + tour.getEstado());
        holder.tvDuracion.setText(tour.getFecha() + " • " + tour.getHora());
        holder.tvPrecio.setText(tour.getPrecio());
        holder.ratingBar.setRating(tour.getRating());
        holder.imgTour.setImageResource(tour.getImagenResId());

        // Ocultar opciones de inicio
        holder.layoutOpciones.setVisibility(View.GONE);

        // Expandir / Colapsar
        holder.itemView.setOnClickListener(v -> {
            if (holder.layoutOpciones.getVisibility() == View.VISIBLE)
                holder.layoutOpciones.setVisibility(View.GONE);
            else
                holder.layoutOpciones.setVisibility(View.VISIBLE);
        });

        // ---- BOTÓN DETALLES ----
        holder.btnDetalles.setOnClickListener(v -> {
            Intent intent = null;

            switch (tour.getEstado().toLowerCase()) {
                case "reservado":
                    intent = new Intent(context, ReservadoActivity.class);
                    break;
                case "en proceso":
                    intent = new Intent(context, EnProcesoActivity.class);
                    break;
                case "finalizado":
                    intent = new Intent(context, FinalizadoActivity.class);
                    break;
            }

            if (intent != null) {
                intent.putExtra("titulo", tour.getTitulo());
                intent.putExtra("fecha", tour.getFecha());
                intent.putExtra("hora", tour.getHora());
                intent.putExtra("precio", tour.getPrecio());
                intent.putExtra("estado", tour.getEstado());
                intent.putExtra("viajeros", tour.getViajeros());
                intent.putExtra("imagen", tour.getImagenResId());
                intent.putExtra("rating", tour.getRating());

                context.startActivity(intent);
            }
        });

        // ---- BOTÓN CHAT ----
        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("estado", tour.getEstado());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaTours.size();
    }

    // ---- FILTRO TEXTO ----
    public void filtrar(String texto) {
        List<HistorialTour> filtrada = new ArrayList<>();

        if (texto.isEmpty()) {
            filtrada.addAll(listaToursOriginal);
        } else {
            for (HistorialTour t : listaToursOriginal) {
                if (t.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                    filtrada.add(t);
                }
            }
        }

        listaTours.clear();
        listaTours.addAll(filtrada);
        notifyDataSetChanged();
    }

    // ---- FILTRO ESTADO ----
    public void filtrarEstado(String estado) {
        List<HistorialTour> filtrada = new ArrayList<>();

        if (estado.isEmpty()) {
            filtrada.addAll(listaToursOriginal);
        } else {
            for (HistorialTour t : listaToursOriginal) {
                if (t.getEstado().equalsIgnoreCase(estado)) {
                    filtrada.add(t);
                }
            }
        }

        listaTours.clear();
        listaTours.addAll(filtrada);
        notifyDataSetChanged();
    }

    // ---- VIEWHOLDER ----
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
