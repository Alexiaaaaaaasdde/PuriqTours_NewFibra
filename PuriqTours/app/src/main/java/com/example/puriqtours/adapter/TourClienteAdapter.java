package com.example.puriqtours.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.TourLegacy;

import java.util.List;

public class TourClienteAdapter extends RecyclerView.Adapter<TourClienteAdapter.TourViewHolder> {

    private List<TourLegacy> tourLegacies;

    public TourClienteAdapter(List<TourLegacy> tourLegacies) {
        this.tourLegacies = tourLegacies;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        TourLegacy tourLegacy = tourLegacies.get(position);
        holder.tourTitle.setText(tourLegacy.getTitle());
        holder.tourLocation.setText(tourLegacy.getLocation());
        holder.tourStatus.setText(tourLegacy.getStatus());

        // Configurar las estrellas según la calificación
        if (tourLegacy.getRating() > 0) {
            holder.tourRating.setText("★★★★★".substring(0, tourLegacy.getRating()));
        } else {
            holder.tourRating.setText("Sin calificar");
        }
    }

    @Override
    public int getItemCount() {
        return tourLegacies.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        TextView tourTitle;
        TextView tourLocation;
        TextView tourStatus;
        TextView tourRating;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            tourTitle = itemView.findViewById(R.id.tvTourTitle);
            tourLocation = itemView.findViewById(R.id.tvTourLocation);
            tourStatus = itemView.findViewById(R.id.tvTourStatus);
            tourRating = itemView.findViewById(R.id.tvTourRating);
        }
    }
}
