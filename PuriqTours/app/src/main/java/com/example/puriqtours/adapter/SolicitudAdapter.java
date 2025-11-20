package com.example.puriqtours.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.util.Log;
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

import com.example.puriqtours.guia.DetallesBottomSheet;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Solicitud;
import com.example.puriqtours.helper.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    private static final String TAG = "SolicitudAdapter";
    private FragmentManager fragmentManager;
    private List<Solicitud> solicitudes;
    private FirestoreHelper firestoreHelper;

    public SolicitudAdapter(List<Solicitud> solicitudes, FragmentManager fragmentManager) {
        this.solicitudes = solicitudes;
        this.fragmentManager = fragmentManager;
        this.firestoreHelper = new FirestoreHelper();
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

        // Usar los campos correctos de la nueva estructura
        holder.tvTitulo.setText(solicitud.getTitle() != null ? solicitud.getTitle() : "Sin título");
        holder.tvDescripcionCorta.setText(solicitud.getDesc() != null ? solicitud.getDesc() : "Sin descripción");
        holder.imgSolicitud.setImageResource(R.drawable.kuelap); // Imagen por defecto
        holder.tvDescripcionCompleta.setText(solicitud.getDesc() != null ? solicitud.getDesc() : "Sin descripción");
        
        // Mostrar pago
        String pagoText = "Pago: S/. " + (solicitud.getPay() != null ? solicitud.getPay().toString() : "0.00");
        holder.tvCiudad.setText(pagoText);
        
        // Mostrar ID de reserva
        holder.tvFecha.setText("Reserva: " + (solicitud.getIdReserva() != null ? solicitud.getIdReserva() : "N/A"));
        
        // Mostrar status
        holder.tvEmpresa.setText("Estado: " + (solicitud.getStatus() != null ? solicitud.getStatus() : "Pendiente"));
        holder.tvRangoHora.setText(""); // Sin hora por ahora

        // Por ahora siempre mostrar expandido
        holder.layoutExpandible.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            // Toggle expandido
            int currentVisibility = holder.layoutExpandible.getVisibility();
            holder.layoutExpandible.setVisibility(currentVisibility == View.VISIBLE ? View.GONE : View.VISIBLE);
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
                // Obtener el UID del guía actual (usuario logueado)
                String guideUid = FirebaseAuth.getInstance().getUid();
                
                if (guideUid == null || solicitud.getIdReserva() == null) {
                    Toast.makeText(v.getContext(), "Error: No se pudo aceptar la solicitud", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                
                // Asignar el guía a la reserva en Firestore
                firestoreHelper.assignGuideToReserva(solicitud.getIdReserva(), guideUid, success -> {
                    if (success) {
                        Toast.makeText(v.getContext(), "Solicitud aceptada ✅", Toast.LENGTH_SHORT).show();
                        
                        // Remover la solicitud de la lista ya que fue aceptada
                        int positionToRemove = holder.getAdapterPosition();
                        if (positionToRemove != RecyclerView.NO_POSITION) {
                            solicitudes.remove(positionToRemove);
                            notifyItemRemoved(positionToRemove);
                            notifyItemRangeChanged(positionToRemove, solicitudes.size());
                        }
                    } else {
                        Toast.makeText(v.getContext(), "Error al aceptar la solicitud", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al asignar guía a reserva: " + solicitud.getIdReserva());
                    }
                    dialog.dismiss();
                });
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
    
    /**
     * Actualizar la lista de solicitudes
     */
    public void updateList(List<Solicitud> newSolicitudes) {
        this.solicitudes = newSolicitudes;
        notifyDataSetChanged();
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
