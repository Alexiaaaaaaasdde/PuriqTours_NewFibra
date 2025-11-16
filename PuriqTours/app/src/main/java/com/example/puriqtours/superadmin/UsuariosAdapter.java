package com.example.puriqtours.superadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import androidx.appcompat.app.AlertDialog;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {
    private List<Usuario> listaUsuarios;
    private Context context;
    public UsuariosAdapter(Context context, List<Usuario> listaUsuarios) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
    }

    public void setUsuarios(List<Usuario> nuevos) {
        this.listaUsuarios = nuevos;
        notifyDataSetChanged();
    }

    public void sortByNameAsc() {
        if (this.listaUsuarios == null) return;
        Collections.sort(this.listaUsuarios, new Comparator<Usuario>() {
            @Override
            public int compare(Usuario u1, Usuario u2) {
                String n1 = u1 != null && u1.nombre != null ? u1.nombre : "";
                String n2 = u2 != null && u2.nombre != null ? u2.nombre : "";
                return n1.compareToIgnoreCase(n2);
            }
        });
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtCiudad, txtEmpresa, txtFechaRegistro;
        LinearLayout layoutBotones;
        RatingBar ratingBar;
        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtCiudad = itemView.findViewById(R.id.txtCiudad);
            txtEmpresa = itemView.findViewById(R.id.txtEmpresa);
            txtFechaRegistro = itemView.findViewById(R.id.txtFechaRegistro);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            layoutBotones = itemView.findViewById(R.id.layoutBotones);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.txtNombre.setText(usuario.nombre);
        holder.txtCiudad.setText(usuario.ciudad);
        holder.txtEmpresa.setVisibility(View.GONE);
        holder.txtFechaRegistro.setVisibility(View.GONE);
        holder.ratingBar.setVisibility(View.GONE);
        holder.layoutBotones.removeAllViews();

        int btnWidth = (int) (holder.itemView.getResources().getDisplayMetrics().density * 110); // 110dp
        int btnHeight = (int) (holder.itemView.getResources().getDisplayMetrics().density * 38); // 38dp

        if (usuario.getTipo() == 0) { // Cliente
            Button btnBloquear = new Button(context);
            btnBloquear.setText("Bloquear");
            btnBloquear.setAllCaps(false);
            btnBloquear.setTextColor(0xFFFFFFFF);
            btnBloquear.setBackgroundResource(R.drawable.bg_oval_button);
            btnBloquear.setTextSize(15);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(btnWidth, btnHeight);
            params1.setMarginEnd(8);
            btnBloquear.setLayoutParams(params1);

            Button btnDesbloquear = new Button(context);
            btnDesbloquear.setText("Desbloquear");
            btnDesbloquear.setAllCaps(false);
            btnDesbloquear.setTextColor(0xFFFFFFFF);
            btnDesbloquear.setBackgroundResource(R.drawable.bg_oval_button);
            btnDesbloquear.setTextSize(15);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(btnWidth, btnHeight);
            btnDesbloquear.setLayoutParams(params2);

            holder.layoutBotones.addView(btnBloquear);
            holder.layoutBotones.addView(btnDesbloquear);
        } else if (usuario.getTipo() == 1) { // Guía
            holder.ratingBar.setVisibility(View.VISIBLE);
            int valoracion = ((UsuarioGuia) usuario).valoracion;
            holder.ratingBar.setRating(valoracion);
            Button btnHabilitar = new Button(context);
            btnHabilitar.setText("Habilitar");
            btnHabilitar.setAllCaps(false);
            btnHabilitar.setTextColor(0xFFFFFFFF);
            btnHabilitar.setBackgroundResource(R.drawable.bg_oval_button);
            btnHabilitar.setTextSize(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(btnWidth, btnHeight);
            btnHabilitar.setLayoutParams(params);
            holder.layoutBotones.addView(btnHabilitar);
        } else if (usuario.getTipo() == 2) { // Administrador
            holder.txtEmpresa.setVisibility(View.VISIBLE);
            holder.txtFechaRegistro.setVisibility(View.VISIBLE);
            holder.txtEmpresa.setText(((UsuarioAdministrador) usuario).empresa);
            holder.txtFechaRegistro.setText("Fecha registro: " + ((UsuarioAdministrador) usuario).fechaRegistro);
            Button btnVerMas = new Button(context);
            btnVerMas.setText("Ver más");
            btnVerMas.setAllCaps(false);
            btnVerMas.setTextColor(0xFFFFFFFF);
            btnVerMas.setBackgroundResource(R.drawable.bg_oval_button);
            btnVerMas.setTextSize(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(btnWidth, btnHeight);
            btnVerMas.setLayoutParams(params);
            holder.layoutBotones.addView(btnVerMas);

            // Mostrar diálogo detallado y scrollable con todos los atributos desde Firestore
            btnVerMas.setOnClickListener(v -> {
                View dlgView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_details, null);
                TextView tvAdminName = dlgView.findViewById(R.id.tvAdminName);
                TextView tvAdminAddress = dlgView.findViewById(R.id.tvAdminAddress);
                TextView tvAdminEmpresa = dlgView.findViewById(R.id.tvAdminEmpresa);
                TextView tvAdminRegistro = dlgView.findViewById(R.id.tvAdminRegistro);
                TextView tvAdminDetails = dlgView.findViewById(R.id.tvAdminDetails);
                Button btnActivateAdmin = dlgView.findViewById(R.id.btnActivateAdmin);
                Button btnDeactivateAdmin = dlgView.findViewById(R.id.btnDeactivateAdmin);

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(dlgView)
                        .create();

                // carga desde Firestore el documento completo para mostrar todos los campos
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String uid = usuario.getUid();
                if (uid == null) uid = usuario.nombre; // fallback si no hay uid
                final String uidFinal = uid;
                db.collection("users").document(uidFinal).get(Source.SERVER)                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            tvAdminName.setText(usuario.nombre);
                            tvAdminDetails.setText("No se encontró el documento completo.");
                        } else {
                            String name = doc.getString("name");
                            if (name == null) name = doc.getString("username");
                            tvAdminName.setText(name != null ? name : usuario.nombre);
                            tvAdminAddress.setText("Address: " + (doc.getString("address") != null ? doc.getString("address") : ""));
                            Object regObj = doc.get("registro");
                            String regStr = "";
                            if (regObj instanceof com.google.firebase.Timestamp) regStr = ((com.google.firebase.Timestamp) regObj).toDate().toString();
                            else if (regObj instanceof java.util.Date) regStr = ((java.util.Date) regObj).toString();
                            else if (regObj != null) regStr = regObj.toString();
                            tvAdminRegistro.setText("Registro: " + regStr);

                            // empresa: si es id, intentar resolver nombre
                            String empresaId = doc.getString("empresa");
                            if (empresaId != null && !empresaId.isEmpty()) {
                                db.collection("empresas").document(empresaId).get(Source.SERVER)
                                    .addOnSuccessListener(ed -> {
                                        String nombreEmpresa = ed.getString("nombre");
                                        tvAdminEmpresa.setText("Empresa: " + (nombreEmpresa != null ? nombreEmpresa : empresaId));
                                    }).addOnFailureListener(e -> tvAdminEmpresa.setText("Empresa: " + empresaId));
                            } else tvAdminEmpresa.setText("Empresa: ");

                            // Construir un listado legible de todos los campos del documento
                            StringBuilder sb = new StringBuilder();
                            if (doc.getData() != null) {
                                for (Map.Entry<String, Object> entry : doc.getData().entrySet()) {
                                    Object val = entry.getValue();
                                    String sval;
                                    if (val instanceof com.google.firebase.Timestamp) sval = ((com.google.firebase.Timestamp) val).toDate().toString();
                                    else if (val instanceof java.util.Date) sval = ((java.util.Date) val).toString();
                                    else sval = val != null ? val.toString() : "";
                                    sb.append(entry.getKey()).append(": ").append(sval).append("\n\n");
                                }
                            }
                            tvAdminDetails.setText(sb.toString());
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvAdminName.setText(usuario.nombre);
                        tvAdminDetails.setText("Error cargando detalles: " + e.getMessage());
                    });

                // Acciones Activar/Desactivar
                btnActivateAdmin.setOnClickListener(btn -> {
                    if (uidFinal != null) {
                        db.collection("users").document(uidFinal).update("state", "habilitado")
                                .addOnSuccessListener(a -> {
                                usuario.setState("habilitado");
                                notifyDataSetChanged();
                                dialog.dismiss();
                            });
                    }
                });
                btnDeactivateAdmin.setOnClickListener(btn -> {
                    if (uidFinal != null) {
                        db.collection("users").document(uidFinal).update("state", "deshabilitado")
                                .addOnSuccessListener(a -> {
                                usuario.setState("deshabilitado");
                                notifyDataSetChanged();
                                dialog.dismiss();
                            });
                    }
                });

                dialog.show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }
}
