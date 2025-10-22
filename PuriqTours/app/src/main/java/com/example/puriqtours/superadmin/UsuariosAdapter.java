package com.example.puriqtours.superadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {
    private List<Usuario> listaUsuarios;
    private Context context;

    public UsuariosAdapter(Context context, List<Usuario> listaUsuarios) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
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
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }
}
