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

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.cliente.ChatActivity;
import com.example.puriqtours.cliente.ReservaDetalleActivity;
import com.example.puriqtours.entity.HistorialTour;

import java.util.ArrayList;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<HistorialTour> listaTours;
    private List<HistorialTour> listaToursOriginal;
    private Context context;

    public HistorialAdapter(List<HistorialTour> listaTours, Context context) {
        this.context = context;
        this.listaTours = new ArrayList<>();
        this.listaToursOriginal = new ArrayList<>();

        if (listaTours != null) {
            this.listaTours.addAll(listaTours);
            this.listaToursOriginal.addAll(listaTours);
        }
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

        holder.tvTitulo.setText(tour.getTitulo());
        holder.tvEstado.setText("Estado: " + tour.getEstado());
        holder.tvDuracion.setText(tour.getFecha() + " • " + tour.getHora());
        holder.tvPrecio.setText(tour.getPrecio());
        holder.ratingBar.setRating(tour.getRating());

        // ✅ Cargar imagen con Glide
        if (tour.getImageUrl() != null && !tour.getImageUrl().isEmpty()) {
            Glide.with(context).load(tour.getImageUrl()).into(holder.imgTour);
        } else {
            holder.imgTour.setImageResource(tour.getImagenResId());
        }

        // ✅ ASEGURAR QUE LOS BOTONES SEAN VISIBLES
        holder.layoutOpciones.setVisibility(View.VISIBLE);
        holder.btnChat.setVisibility(View.VISIBLE);
        holder.btnDetalles.setVisibility(View.VISIBLE);

        // ✅ Click en botón Detalles
        holder.btnDetalles.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReservaDetalleActivity.class);
            intent.putExtra("RESERVA_ID", tour.getIdReserva());
            context.startActivity(intent);
        });

        // ✅ Click en botón Chat
        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("RESERVA_ID", tour.getIdReserva());
            intent.putExtra("TOUR_TITULO", tour.getTitulo());
            intent.putExtra("estado", tour.getEstado());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaTours.size();
    }

    public void actualizarLista(List<HistorialTour> nuevaLista) {
        listaTours.clear();
        listaToursOriginal.clear();
        listaTours.addAll(nuevaLista);
        listaToursOriginal.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        List<HistorialTour> filtrada = new ArrayList<>();

        for (HistorialTour t : listaToursOriginal) {
            if (t.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                filtrada.add(t);
            }
        }

        listaTours.clear();
        listaTours.addAll(filtrada);
        notifyDataSetChanged();
    }

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvEstado, tvDuracion, tvPrecio;
        RatingBar ratingBar;
        ImageView imgTour;
        LinearLayout layoutOpciones;
        Button btnChat, btnDetalles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitulo = itemView.findViewById(R.id.tvTitulo);
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