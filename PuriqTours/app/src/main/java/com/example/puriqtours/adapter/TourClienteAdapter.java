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

    public TourClienteAdapter(List<Tour> tourList) {
        this.tourList = tourList;
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

        holder.tourTitle.setText(tour.getTitle());
        holder.tourLocation.setText(tour.getLocation());
        holder.tourStatus.setText(tour.getStatus());
        holder.tourDesc.setText(tour.getDesc());

        // ⭐ Rating
        if (tour.getRating() != null && tour.getRating() > 0) {
            holder.tourRating.setText("★★★★★".substring(0, tour.getRating()));
        } else {
            holder.tourRating.setText("Sin calificar");
        }

        // ⭐ Imagen
        if (tour.getImageUrl() != null && !tour.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(tour.getImageUrl())
                    .placeholder(R.drawable.kuelap)
                    .into(holder.tourImage);
        } else {
            holder.tourImage.setImageResource(R.drawable.kuelap);
        }
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public void updateList(List<Tour> newList) {
        this.tourList = newList;
        notifyDataSetChanged();
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
    }
}
