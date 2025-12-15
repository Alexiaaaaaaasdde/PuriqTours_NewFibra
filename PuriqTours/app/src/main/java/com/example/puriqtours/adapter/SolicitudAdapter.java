package com.example.puriqtours.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    private final FragmentManager fragmentManager;
    private final List<Solicitud> solicitudes;

    private String estadoFiltro = "Todos";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        // --------------------------
        // 🔹 Adaptación a nueva estructura
        // --------------------------
        holder.tvTitulo.setText(solicitud.getTitle());
        holder.tvDescripcionCorta.setText(solicitud.getDesc());
        holder.tvDescripcionCompleta.setText(solicitud.getDesc());
        holder.tvPay.setText("Paga: S/ " + solicitud.getPay());
        holder.tvStatus.setText("Estado: " + solicitud.getStatus());

        // --------------------------
        // 🔹 Cargar imagen del tour
        // --------------------------
        Glide.with(holder.itemView.getContext())
                .load(solicitud.getImageUrl()) // URL obtenida desde el documento "tours"
                .placeholder(R.drawable.placeholder_img)
                .error(R.drawable.placeholder_img)
                .into(holder.imgSolicitud);

        // --------------------------
        // 🔹 Control de expansión
        // --------------------------
        boolean expandido = solicitud.isExpandido();
        holder.layoutExpandible.setVisibility(expandido ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            solicitud.setExpandido(!solicitud.isExpandido());
            notifyItemChanged(position);
        });

        // --------------------------
        // 🔹 Filtro visual por estado
        // --------------------------
        if (!estadoFiltro.equals("Todos")) {
            if (!solicitud.getStatus().equals(estadoFiltro)) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                return;
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
            }
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }

        // --------------------------
        // 🔹 Botón Detalles
        // --------------------------
        holder.btnDetalles.setOnClickListener(v -> {
            DetallesBottomSheet sheet = new DetallesBottomSheet(solicitud.getIdTour(), solicitud.getPay());
            sheet.show(fragmentManager, "DetallesTour");
        });

        // --------------------------
        // 🔹 Verificación de Estado
        // --------------------------

        String estado = solicitud.getStatus(); // o getEstado(), según tu modelo

        if (estado != null && estado.equals("Pendiente")) {
            holder.btnAceptar.setVisibility(View.VISIBLE);
            holder.btnRechazar.setVisibility(View.VISIBLE);
        } else {
            holder.btnAceptar.setVisibility(View.GONE);
            holder.btnRechazar.setVisibility(View.GONE);
        }

        // --------------------------
        // 🔹 Acción botón Aceptar
        // --------------------------
        holder.btnAceptar.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_aceptar);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Button btnAceptar = dialog.findViewById(R.id.btnAceptar);
            Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

            btnAceptar.setOnClickListener(view -> {
                db.collection("solicitudes")
                        .document(solicitud.getIdSolicitud())
                        .update("status", "Aceptado")
                        .addOnSuccessListener(aVoid -> {

                            // 2️⃣ Agregar idGuia al documento de tours/{idTour}
                            db.collection("tours")
                                    .document(solicitud.getIdTour())
                                    .update("idGuia", solicitud.getIdGuia())
                                    .addOnSuccessListener(x -> {
                                        Toast.makeText(v.getContext(),
                                                "Solicitud aceptada",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(v.getContext(),
                                                "Error agregando idGuia: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });

                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(),
                                        "Error actualizando solicitud: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
                dialog.dismiss();
            });
            btnCancelar.setOnClickListener(view -> dialog.dismiss());
            dialog.show();

        });


        // --------------------------
        // 🔹 Acción botón Rechazar
        // --------------------------
        holder.btnRechazar.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_rechazar);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Button btnRechazar = dialog.findViewById(R.id.btnRechazar);
            Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

            btnRechazar.setOnClickListener(view -> {
                db.collection("solicitudes")
                        .document(solicitud.getIdSolicitud())
                        .update("status", "Rechazado")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(v.getContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(v.getContext(),
                                    "Error en rechazar la solicitud.",
                                    Toast.LENGTH_LONG).show();
                        });;
                dialog.dismiss();
            });

            btnCancelar.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return solicitudes != null ? solicitudes.size() : 0;
    }

    public void setEstadoFiltro(String estado) {
        this.estadoFiltro = estado;
        notifyDataSetChanged();
    }

    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgSolicitud;
        TextView tvTitulo, tvDescripcionCorta, tvDescripcionCompleta, tvPay, tvStatus;
        LinearLayout layoutExpandible;
        Button btnDetalles, btnAceptar, btnRechazar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSolicitud = itemView.findViewById(R.id.imgSolicitud);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcionCorta = itemView.findViewById(R.id.tvDescripcionCorta);
            tvDescripcionCompleta = itemView.findViewById(R.id.tvDescripcionCompleta);

            tvPay = itemView.findViewById(R.id.tvPay);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            layoutExpandible = itemView.findViewById(R.id.layoutExpandible);

            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}
