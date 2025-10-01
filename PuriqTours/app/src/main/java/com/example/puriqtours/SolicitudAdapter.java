package com.example.puriqtours;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    private FragmentManager fragmentManager;
    private List<Solicitud> solicitudes;

    public SolicitudAdapter(List<Solicitud> solicitudes, FragmentManager fragmentManager) {
        this.solicitudes = solicitudes;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new SolicitudViewHolder(vista);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        Solicitud solicitud = solicitudes.get(position);

        holder.tvTitulo.setText(solicitud.getTitulo());
        holder.tvDescripcionCorta.setText(solicitud.getDescripcion());
        holder.imgSolicitud.setImageResource(solicitud.getImagenResId());
        holder.tvDescripcionCompleta.setText(solicitud.getDescripcion());
        holder.tvCiudad.setText("Ciudad: " + solicitud.getCiudad());
        holder.tvFecha.setText("Fecha: " + solicitud.getFecha());
        holder.tvEmpresa.setText(solicitud.getEmpresa());
        holder.tvRangoHora.setText("Hora: " + solicitud.getHoraInicio() + "-" + solicitud.getHoraFin());

        boolean expandido = solicitud.isExpandido();
        holder.layoutExpandible.setVisibility(expandido ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            solicitud.setExpandido(!solicitud.isExpandido());
            notifyItemChanged(position);
        });

        holder.btnDetalles.setOnClickListener(v -> {
            DetallesBottomSheet bottomSheet = new DetallesBottomSheet();
            bottomSheet.show(fragmentManager, bottomSheet.getTag());
        });

        // 🔹 Acción botón Aceptar
        holder.btnAceptar.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_aceptar); // Tu XML modificado
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // 🔹 Referencias a botones
            Button btnAceptar = dialog.findViewById(R.id.btnAceptar);
            Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

            btnAceptar.setOnClickListener(view -> {
                Toast.makeText(v.getContext(), "Solicitud aceptada ✅", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnCancelar.setOnClickListener(view -> dialog.dismiss());

            dialog.show();

            // 🔹 Ajustar ancho al máximo después de mostrarlo
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
            dialog.setContentView(R.layout.dialog_rechazar); // Tu XML modificado
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // 🔹 Referencias a botones
            Button btnAceptar = dialog.findViewById(R.id.btnRechazar);
            Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

            btnAceptar.setOnClickListener(view -> {
                Toast.makeText(v.getContext(), "Solicitud Rechazada ❌", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnCancelar.setOnClickListener(view -> dialog.dismiss());

            dialog.show();

            // 🔹 Ajustar ancho al máximo después de mostrarlo
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
        return solicitudes.size();
    }

    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSolicitud;
        TextView tvTitulo, tvDescripcionCorta, tvDescripcionCompleta, tvCiudad, tvFecha, tvRangoHora, tvEmpresa;
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
