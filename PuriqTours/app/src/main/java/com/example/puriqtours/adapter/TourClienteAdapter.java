package com.example.puriqtours.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;

import java.util.List;
public class TourClienteAdapter extends RecyclerView.Adapter<TourClienteAdapter.TourViewHolder> {

    private List<Tour> tourList;
    private OnTourClickListener listener;

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
    }

    public TourClienteAdapter(List<Tour> tourList, OnTourClickListener listener) {
        this.tourList = tourList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tourList.get(position);

        holder.bind(tour, listener);
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {

        ImageView tourImage;
        TextView tourTitle, tourLocation, tourStatus, tourRating, tourDesc;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);

            tourImage = itemView.findViewById(R.id.imgTour);
            tourTitle = itemView.findViewById(R.id.tvTourTitle);
            tourLocation = itemView.findViewById(R.id.tvTourLocation);
            tourStatus  = itemView.findViewById(R.id.tvTourStatus);
            tourRating  = itemView.findViewById(R.id.tvTourRating);
            tourDesc    = itemView.findViewById(R.id.tvTourDesc);
        }

        public void bind(Tour tour, OnTourClickListener listener) {
            // ✅ Manejar valores null
            tourTitle.setText(tour.getTitle() != null ? tour.getTitle() : "Sin título");
            tourLocation.setText(tour.getLocation() != null ? tour.getLocation() : "Sin ubicación");
            tourStatus.setText(tour.getStatus() != null ? tour.getStatus() : "disponible");
            tourDesc.setText(tour.getDesc() != null ? tour.getDesc() : "Sin descripción");

            if (tour.getRating() != null && tour.getRating() > 0) {
                tourRating.setText("★★★★★".substring(0, Math.min(tour.getRating(), 5)));
            } else {
                tourRating.setText("Sin calificar");
            }

            if (tour.getImageUrl() != null && !tour.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(tour.getImageUrl())
                        .placeholder(R.drawable.kuelap)
                        .error(R.drawable.kuelap)
                        .into(tourImage);
            } else {
                tourImage.setImageResource(R.drawable.kuelap);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTourClick(tour);
                }
            });
        }
    }
}
