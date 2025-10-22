package com.example.puriqtours;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<String> mensajes;

    public ChatAdapter(List<String> mensajes) {
        this.mensajes = mensajes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvMensaje.setText(mensajes.get(position));
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        ViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(android.R.id.text1);
        }
    }
}
