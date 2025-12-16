package com.example.puriqtours.superadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class SolicitudesAdapter
        extends RecyclerView.Adapter<SolicitudesAdapter.ViewHolder> {

    private final Context context;
    private final List<DocumentSnapshot> lista;
    private final boolean mostrarBotones;

    public SolicitudesAdapter(Context context, List<DocumentSnapshot> lista, boolean mostrarBotones) {
        this.context = context;
        this.lista = lista;
        this.mostrarBotones = mostrarBotones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_solicitud_guia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = lista.get(position);

        String nombre = doc.getString("name");
        String apellido = doc.getString("last_name");
        String email = doc.getString("email");

        holder.txtNombre.setText(nombre + " " + apellido);
        holder.txtEmail.setText(email);

        // Limpiar antes de añadir
        holder.layoutBotones.removeAllViews();

        if (mostrarBotones) {
            // ==========================
            // BOTÓN ACEPTAR
            // ==========================
            MaterialButton btnAceptar = crearBoton("Aceptar", R.color.green_600);
            btnAceptar.setOnClickListener(v -> {
                doc.getReference()
                        .update("guide_status", "Habilitado")
                        .addOnSuccessListener(a -> {
                            Toast.makeText(context, "Guía habilitado", Toast.LENGTH_SHORT).show();
                            eliminarDeLista(position);
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Error al habilitar", Toast.LENGTH_SHORT).show());
            });

            // ==========================
            // BOTÓN RECHAZAR
            // ==========================
            MaterialButton btnRechazar = crearBoton("Rechazar", R.color.red_600);
            btnRechazar.setOnClickListener(v -> {
                doc.getReference()
                        .update("guide_status", "Rechazado")
                        .addOnSuccessListener(a -> {
                            Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show();
                            eliminarDeLista(position);
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Error al rechazar", Toast.LENGTH_SHORT).show());
            });

            holder.layoutBotones.addView(btnAceptar);
            holder.layoutBotones.addView(btnRechazar);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtEmail;
        LinearLayout layoutBotones;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            layoutBotones = itemView.findViewById(R.id.layoutBotones);
        }
    }

    private MaterialButton crearBoton(String texto, int colorFondo) {
        MaterialButton btn = new MaterialButton(context);

        // Texto
        btn.setText(texto);
        btn.setAllCaps(false);
        btn.setTextSize(14);
        btn.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.setTextColor(context.getColor(android.R.color.white));

        // 🎨 Fondo sólido
        btn.setBackgroundTintList(
                context.getColorStateList(colorFondo)
        );

        // Forma
        btn.setCornerRadius(40);
        btn.setElevation(4);

        // Tamaño
        btn.setMinHeight(36);
        btn.setMinimumHeight(36);
        btn.setMinWidth(120);              // 👈 CLAVE
        btn.setMinimumWidth(120);
        // Margen entre botones
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        params.setMarginEnd(24);
        btn.setLayoutParams(params);

        return btn;
    }


    private void eliminarDeLista(int position) {
        if (position >= 0 && position < lista.size()) {
            lista.remove(position);
            notifyItemRemoved(position);
        }
    }
}
