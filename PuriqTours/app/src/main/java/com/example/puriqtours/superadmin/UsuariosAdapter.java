package com.example.puriqtours.superadmin;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;
    private List<Usuario> listaUsuariosOriginal;
    private Context context;

    public UsuariosAdapter(Context context, List<Usuario> listaUsuarios) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
        this.listaUsuariosOriginal = new ArrayList<>(listaUsuarios);
    }

    public void setUsuarios(List<Usuario> nuevos) {
        this.listaUsuarios = nuevos;
        this.listaUsuariosOriginal = new ArrayList<>(nuevos);
        notifyDataSetChanged();
    }

    private int dpToPx(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale);
    }

    private LinearLayout.LayoutParams paramsBoton() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        params.setMargins(0, 0, dpToPx(8), 0);
        return params;
    }


    public void sortByNameAsc() {
        Collections.sort(listaUsuarios, (u1, u2) -> {
            String n1 = u1.getName() != null ? u1.getName() : "";
            String n2 = u2.getName() != null ? u2.getName() : "";
            return n1.compareToIgnoreCase(n2);
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
        TextView txtNombre;
        ImageView imgAvatar; // 🔹 agregado
        LinearLayout layoutBotones;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            imgAvatar = itemView.findViewById(R.id.imgAvatar); // 🔹 agregado
            layoutBotones = itemView.findViewById(R.id.layoutBotones);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {

        Usuario usuario = listaUsuarios.get(position);

        holder.txtNombre.setText(usuario.getName());

// 🟢 NUEVO BLOQUE — carga la imagen de perfil del usuario
        if (usuario.getProfile_image() != null && !usuario.getProfile_image().isEmpty()) {
            Glide.with(context)
                    .load(usuario.getProfile_image())
                    .placeholder(R.drawable.avatar1) // mientras carga
                    .error(R.drawable.avatar1)       // si falla
                    .circleCrop()                    // forma circular
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.avatar1);
        }

        holder.layoutBotones.removeAllViews();


        String rol = usuario.getRol();
        String estado = usuario.getStatus();

        // -------------------- BOTÓN VER DETALLES --------------------
        Button btnDetalles = new Button(context);
        btnDetalles.setText("Detalles");
        btnDetalles.setAllCaps(false);
        btnDetalles.setBackgroundResource(R.drawable.bg_oval_button);
        btnDetalles.setTextColor(Color.WHITE);
        btnDetalles.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_eye, 0, 0, 0);
        btnDetalles.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        btnDetalles.setLayoutParams(paramsBoton());
        btnDetalles.setGravity(Gravity.CENTER);

        btnDetalles.setOnClickListener(v -> {
            DetallesUsuarioBottomSheet bottomSheet = new DetallesUsuarioBottomSheet(usuario);
            bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "DetallesUsuario");
        });

        btnDetalles.setLayoutParams(paramsBoton());
        holder.layoutBotones.addView(btnDetalles);

        // -------------------- CLIENTE / GUIA / ADMIN --------------------
        if (rol.equalsIgnoreCase("cliente") ||
                rol.equalsIgnoreCase("guia") ||
                rol.equalsIgnoreCase("admin")) {

            if ("Activo".equalsIgnoreCase(estado)) {

                // ---- BLOQUEAR ----
                Button btnBloq = new Button(context);
                btnBloq.setText("Bloquear");
                btnBloq.setAllCaps(false);
                btnBloq.setBackgroundResource(R.drawable.bg_oval_button);
                btnBloq.getBackground().mutate().setTint(Color.parseColor("#E63127"));
                btnBloq.setTextColor(Color.WHITE);
                btnBloq.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_block, 0, 0, 0);
                btnBloq.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
                btnBloq.setLayoutParams(paramsBoton());
                btnBloq.setGravity(Gravity.CENTER);


                btnBloq.setOnClickListener(v -> {
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(usuario.getUid())
                            .update("status", "Inactivo")
                            .addOnSuccessListener(a -> {
                                usuario.setStatus("Inactivo");
                                notifyItemChanged(position);
                                Toast.makeText(context, "Usuario bloqueado", Toast.LENGTH_SHORT).show();
                            });
                });

                btnBloq.setLayoutParams(paramsBoton());
                holder.layoutBotones.addView(btnBloq);

            } else {

                // ---- DESBLOQUEAR ----
                Button btnDes = new Button(context);
                btnDes.setText("Activar");
                btnDes.setAllCaps(false);
                btnDes.setBackgroundResource(R.drawable.bg_oval_button);
                btnDes.getBackground().mutate().setTint(Color.parseColor("#3133E0"));
                btnDes.setTextColor(Color.WHITE);
                btnDes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlock, 0, 0, 0);
                btnDes.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
                btnDes.setLayoutParams(paramsBoton());
                btnDes.setGravity(Gravity.CENTER);


                btnDes.setOnClickListener(v -> {
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(usuario.getUid())
                            .update("status", "Activo")
                            .addOnSuccessListener(a -> {
                                usuario.setStatus("Activo");
                                notifyItemChanged(position);
                                Toast.makeText(context, "Usuario desbloqueado", Toast.LENGTH_SHORT).show();
                            });
                });

                btnDes.setLayoutParams(paramsBoton());
                holder.layoutBotones.addView(btnDes);
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public void filter(String text) {
        listaUsuarios.clear();

        if (text == null || text.trim().isEmpty()) {
            listaUsuarios.addAll(listaUsuariosOriginal);
        } else {
            String f = text.toLowerCase();
            for (Usuario u : listaUsuariosOriginal) {
                if ((u.getName() != null && u.getName().toLowerCase().contains(f)) ||
                        (u.getAddress() != null && u.getAddress().toLowerCase().contains(f))) {
                    listaUsuarios.add(u);
                }
            }
        }

        notifyDataSetChanged();
    }
}
