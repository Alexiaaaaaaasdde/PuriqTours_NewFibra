package com.example.puriqtours;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.entity.Tour;

import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private List<Tour> tours;

    public TourAdapter(List<Tour> tours) {
        this.tours = tours;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tours.get(position);
        holder.tourTitle.setText(tour.getTitle());
        holder.tourLocation.setText(tour.getLocation());
        holder.tourStatus.setText(tour.getStatus());

        // Configurar las estrellas según la calificación
        if (tour.getRating() > 0) {
            holder.tourRating.setText("★★★★★".substring(0, tour.getRating()));
        } else {
            holder.tourRating.setText("Sin calificar");
        }
    }

    @Override
    public int getItemCount() {
        return tours.size();
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