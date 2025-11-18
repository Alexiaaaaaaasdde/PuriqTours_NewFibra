package com.example.puriqtours.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.guia.DetallesBottomSheet;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.TourGuia;
import com.example.puriqtours.guia.DetallesReservaBottomSheet;
import com.example.puriqtours.guia.IniciarTourActivity;

import java.util.List;
import android.content.Context;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class TourGuiaAdapter extends RecyclerView.Adapter<TourGuiaAdapter.ViewHolder> {

    private Context context;
    private List<TourGuia> tourList;
    private OnTourActionListener listener;
    TextView tvDescripcionCompleta;


    // 🔹 Interfaz para el botón “Iniciar tour”
    public interface OnTourActionListener {
        void onIniciar(TourGuia t);
    }

    public TourGuiaAdapter(Context context, List<TourGuia> tourList, OnTourActionListener listener) {
        this.context = context;
        this.tourList = tourList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TourGuiaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_tour_guia, parent, false);
        return new TourGuiaAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourGuiaAdapter.ViewHolder holder, int position) {
        TourGuia t = tourList.get(position);

        // 🔹 Datos principales del tour
        holder.tvTitulo.setText(t.getName());
        holder.tvDescripcionCorta.setText(t.getDesc());
        holder.tvDescripcionCompleta.setText(t.getDesc());
        holder.tvCiudad.setText("Ciudad: " + t.getLocation());
        holder.tvFecha.setText("Fecha: " + t.getDate());
        holder.tvRangoHora.setText("Hora: " + t.getStartTime() + " - " + t.getEndTime());

        // 🔹 Cargar imagen con Glide
        Glide.with(context)
                .load(t.getImg())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgSolicitud);

        // 🔹 Expandible
        holder.itemView.setOnClickListener(v -> {
            boolean visible = holder.layoutExpandible.getVisibility() == View.VISIBLE;
            holder.layoutExpandible.setVisibility(visible ? View.GONE : View.VISIBLE);
        });

        // 🔹 Abrir BOTTOMSHEET con los DETALLES del tour
        holder.btnDetalles.setOnClickListener(v -> {
            DetallesReservaBottomSheet sheet = new DetallesReservaBottomSheet(t);
            sheet.show(
                    ((FragmentActivity) context).getSupportFragmentManager(),
                    "DetallesBottomSheet"
            );
        });

        // 🔹 Obtener estado del tour
        String estado = t.getStatus();

        // 🔹 Control del botón según estado
        if (estado == null) estado = "Reservado";

        switch (estado) {

            case "Reservado":
                holder.btnIniciar.setEnabled(true);
                holder.btnIniciar.setAlpha(1f);
                holder.btnIniciar.setText("Iniciar");
                break;

            case "En proceso":
                holder.btnIniciar.setEnabled(true);
                holder.btnIniciar.setAlpha(1f);
                holder.btnIniciar.setText("Continuar");
                break;

            case "Finalizado":
                holder.btnIniciar.setEnabled(false);
                holder.btnIniciar.setAlpha(0.5f);
                holder.btnIniciar.setText("Finalizado");
                break;
        }

        // 🔹 Acciones Iniciar
        holder.btnIniciar.setOnClickListener(v -> {
            if (listener != null) listener.onIniciar(t);
        });
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgSolicitud;
        TextView tvTitulo, tvDescripcionCorta, tvDescripcionCompleta, tvCiudad, tvFecha, tvRangoHora;
        LinearLayout layoutExpandible;
        MaterialButton btnDetalles, btnIniciar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSolicitud        = itemView.findViewById(R.id.imgSolicitud);
            tvTitulo            = itemView.findViewById(R.id.tvTitulo);
            tvDescripcionCorta  = itemView.findViewById(R.id.tvDescripcionCorta);
            tvCiudad            = itemView.findViewById(R.id.tvCiudad);
            tvFecha             = itemView.findViewById(R.id.tvFecha);
            tvRangoHora         = itemView.findViewById(R.id.tvRangoHora);
            layoutExpandible    = itemView.findViewById(R.id.layoutExpandible);
            tvDescripcionCompleta = itemView.findViewById(R.id.tvDescripcionCompleta);


            btnDetalles         = itemView.findViewById(R.id.btnDetalles);
            btnIniciar          = itemView.findViewById(R.id.btnIniciar);
        }
    }
}

