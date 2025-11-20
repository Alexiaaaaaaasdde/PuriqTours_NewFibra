package com.example.puriqtours.superadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GuiasHorizontalAdapter extends RecyclerView.Adapter<GuiasHorizontalAdapter.GuiaViewHolder> {

    private List<Usuario> guias;   // ← YA CORREGIDO
    private Context context;

    public GuiasHorizontalAdapter(Context context, List<Usuario> guias) {
        this.context = context;
        this.guias = guias;
    }

    public void setGuias(List<Usuario> guias) {
        this.guias = guias;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GuiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guia_horizontal, parent, false);
        return new GuiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuiaViewHolder holder, int position) {

        Usuario guia = guias.get(position);

        // Nombre correcto del campo
        holder.txtGuiaNombre.setText(guia.getName());

        // Imagen correcta del campo
        if (guia.getProfile_image() != null && !guia.getProfile_image().isEmpty()) {
            Picasso.get()
                    .load(guia.getProfile_image())
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.avatar1)
                    .into(holder.imgGuiaAvatar);
        } else {
            holder.imgGuiaAvatar.setImageResource(R.drawable.avatar1);
        }
    }

    @Override
    public int getItemCount() {
        return guias.size();
    }

    public static class GuiaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGuiaAvatar;
        TextView txtGuiaNombre;

        public GuiaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGuiaAvatar = itemView.findViewById(R.id.imgGuiaAvatar);
            txtGuiaNombre = itemView.findViewById(R.id.txtGuiaNombre);
        }
    }
}
