package com.example.puriqtours.superadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;

import java.util.List;
import java.util.Map;


public class TopGuiasAdapter extends RecyclerView.Adapter<TopGuiasAdapter.GuiaViewHolder> {

    private List<Usuario> listaGuias;
    private Map<String, Integer> conteoGuias;
    private Context context;

    public TopGuiasAdapter(List<Usuario> listaGuias, Map<String, Integer> conteoGuias) {
        this.listaGuias = listaGuias;
        this.conteoGuias = conteoGuias;
    }

    @NonNull
    @Override
    public GuiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guia_top, parent, false);
        context = parent.getContext();
        return new GuiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuiaViewHolder holder, int position) {
        Usuario guia = listaGuias.get(position);

        holder.tvNombre.setText(guia.getName() + " " + guia.getLast_name());

        int total = conteoGuias.get(guia.getUid());
        holder.tvSolicitudes.setText("Tours atendidos: " + total);

        holder.tvEmail.setText(guia.getEmail());

        // Rating dinámico
        double rating = guia.getRating(); // ⭐ YA NO ES HARDCODEADO
        holder.tvRating.setText(String.format("%.1f", rating));

        // Foto
        if (guia.getProfile_image() != null && !guia.getProfile_image().isEmpty()) {
            Glide.with(context).load(guia.getProfile_image()).into(holder.imgGuia);
        } else {
            holder.imgGuia.setImageResource(R.drawable.avatar1);
        }
    }

    @Override
    public int getItemCount() {
        return listaGuias.size();
    }

    public static class GuiaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGuia;
        TextView tvNombre, tvSolicitudes, tvEmail, tvRating;

        public GuiaViewHolder(@NonNull View itemView) {
            super(itemView);

            imgGuia = itemView.findViewById(R.id.imgGuia);
            tvNombre = itemView.findViewById(R.id.tvGuiaNombre);
            tvSolicitudes = itemView.findViewById(R.id.tvGuiaSolicitudes);
            tvEmail = itemView.findViewById(R.id.tvGuiaEmail);
            tvRating = itemView.findViewById(R.id.tvGuiaRating); // ⭐ ¡NUEVO!
        }
    }

}

