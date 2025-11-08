package com.example.puriqtours.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Solicitud;
import com.example.puriqtours.guia.DetallesBottomSheet;

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    private final FragmentManager fragmentManager;
    private final List<Solicitud> solicitudes;

    public SolicitudAdapter(List<Solicitud> solicitudes, FragmentManager fragmentManager) {
        this.solicitudes = solicitudes;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud, parent, false);
        return new SolicitudViewHolder(vista);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        Solicitud solicitud = solicitudes.get(position);

        // --- Asignar valores recuperados de Firestore ---
        holder.tvTitulo.setText(solicitud.getTitulo());
        holder.tvDescripcionCorta.setText(solicitud.getDescripcion());
        holder.tvDescripcionCompleta.setText(solicitud.getDescripcion());
        holder.tvCiudad.setText("Ciudad: " + solicitud.getCiudad());
        holder.tvFecha.setText("Fecha: " + solicitud.getFecha());
        holder.tvEmpresa.setText(solicitud.getEmpresa());
        holder.tvRangoHora.setText("Hora: " + solicitud.getHoraInicio() + " - " + solicitud.getHoraFin());

        // 🔹 Cargar imagen desde Firebase Storage / URL
        Glide.with(holder.itemView.getContext())
                .load(solicitud.getImagenUrl()) // campo adaptado para Firestore
                .placeholder(R.drawable.placeholder_img)
                .error(R.drawable.placeholder_img)
                .into(holder.imgSolicitud);

        // 🔹 Control de expansión del item
        boolean expandido = solicitud.isExpandido();
        holder.layoutExpandible.setVisibility(expandido ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            solicitud.setExpandido(!solicitud.isExpandido());
            notifyItemChanged(position);
        });

        // 🔹 Mostrar detalles en BottomSheet
        holder.btnDetalles.setOnClickListener(v -> {
            DetallesBottomSheet bottomSheet = new DetallesBottomSheet();
            bottomSheet.show(fragmentManager, bottomSheet.getTag());
        });

        // 🔹 Acción botón Aceptar
        holder.btnAceptar.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_aceptar);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Button btnAceptar = dialog.findViewById(R.id.btnAceptar);
            Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

            btnAceptar.setOnClickListener(view -> {
                Toast.makeText(v.getContext(), "Solicitud aceptada ✅", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnCancelar.setOnClickListener(view -> dialog.dismiss());
            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });

        // 🔹 Acción botón Rechazar
        holder.btnRechazar.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_rechazar);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Button btnAceptar = dialog.findViewById(R.id.btnRechazar);
            Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

            btnAceptar.setOnClickListener(view -> {
                Toast.makeText(v.getContext(), "Solicitud rechazada ❌", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnCancelar.setOnClickListener(view -> dialog.dismiss());
            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return solicitudes != null ? solicitudes.size() : 0;
    }

    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSolicitud;
        TextView tvTitulo, tvDescripcionCorta, tvDescripcionCompleta,
                tvCiudad, tvFecha, tvRangoHora, tvEmpresa;
        LinearLayout layoutExpandible;
        Button btnDetalles, btnAceptar, btnRechazar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSolicitud = itemView.findViewById(R.id.imgSolicitud);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcionCorta = itemView.findViewById(R.id.tvDescripcionCorta);
            tvDescripcionCompleta = itemView.findViewById(R.id.tvDescripcionCompleta);
            tvCiudad = itemView.findViewById(R.id.tvCiudad);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEmpresa = itemView.findViewById(R.id.tvEmpresa);
            tvRangoHora = itemView.findViewById(R.id.tvRangoHora);
            layoutExpandible = itemView.findViewById(R.id.layoutExpandible);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}
