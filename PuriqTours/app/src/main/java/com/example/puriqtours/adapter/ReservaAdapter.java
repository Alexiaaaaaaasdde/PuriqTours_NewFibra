package com.example.puriqtours.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.admin.ReservaDetailActivity;
import com.example.puriqtours.entity.Reserva;

import java.util.ArrayList;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    private Context context;
    private List<Reserva> reservaList;
    private List<Reserva> reservaListFull; // Para búsqueda

    public ReservaAdapter(Context context, List<Reserva> reservaList) {
        this.context = context;
        this.reservaList = reservaList;
        this.reservaListFull = new ArrayList<>(reservaList);
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reserva_card, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservaList.get(position);

        holder.tvTourName.setText(reserva.getTitle() != null ? reserva.getTitle() : "Sin título");
        holder.tvFecha.setText(reserva.getDate() != null ? reserva.getDate() : "Sin fecha");
        holder.tvHora.setText(reserva.getHour() != null ? reserva.getHour() : "Sin hora");
        holder.tvStatus.setText(reserva.getStatus() != null ? reserva.getStatus() : "Pendiente");
        holder.tvPrecio.setText(reserva.getPrice() != null ? "S/ " + reserva.getPrice() : "S/ 0");
        holder.tvTravelers.setText(reserva.getTravelers() != null ? reserva.getTravelers() : "N/A");

        // Cambiar color del status según estado
        String status = reserva.getStatus();
        if ("Finalizado".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.green));
        } else if ("En proceso".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.teal_700));
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.orange));
        }

        // Click en la card para ver detalles
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReservaDetailActivity.class);
            intent.putExtra("reserva", reserva);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reservaList.size();
    }

    // Método para filtrar reservas
    public void filter(String query) {
        reservaList.clear();
        if (query.isEmpty()) {
            reservaList.addAll(reservaListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Reserva reserva : reservaListFull) {
                if (reserva.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    reserva.getDate().toLowerCase().contains(lowerCaseQuery) ||
                    reserva.getStatus().toLowerCase().contains(lowerCaseQuery)) {
                    reservaList.add(reserva);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Método para actualizar la lista completa
    public void updateList(List<Reserva> newList) {
        reservaList.clear();
        reservaList.addAll(newList);
        reservaListFull.clear();
        reservaListFull.addAll(newList);
        notifyDataSetChanged();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTourName, tvFecha, tvHora, tvStatus, tvPrecio, tvTravelers;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewReserva);
            tvTourName = itemView.findViewById(R.id.tvTourName);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvTravelers = itemView.findViewById(R.id.tvTravelers);
        }
    }
}
