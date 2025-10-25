package com.example.puriqtours.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.puriqtours.R;
import com.example.puriqtours.TourDetailActivity;
import com.example.puriqtours.model.Tour;
import java.util.ArrayList;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> implements Filterable {

    private Context context;
    private List<Tour> tourList;
    private List<Tour> tourListFiltered;
    private OnTourClickListener onTourClickListener;

    public interface OnTourClickListener {
        void onTourClick(Tour tour, int position);
    }

    public TourAdapter(Context context, List<Tour> tourList) {
        this.context = context;
        this.tourList = tourList;
        this.tourListFiltered = new ArrayList<>(tourList);
    }

    public void setOnTourClickListener(OnTourClickListener listener) {
        this.onTourClickListener = listener;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tourListFiltered.get(position);
        
        holder.tourTitle.setText(tour.getName());
        holder.tourDescription.setText(tour.getDescription());
        holder.tourLocation.setText(tour.getLocation());
        holder.tourPrice.setText("S/ " + tour.getPrice());
        holder.tourDuration.setText(tour.getDurationText());
        
        if (tour.getImageResource() != 0) {
            holder.tourImage.setImageResource(tour.getImageResource());
        } else {
            holder.tourImage.setImageResource(R.drawable.kuelap);
        }

        holder.tourStatus.setText(tour.getStatus());
        if (tour.getGuideAssigned() != null && !tour.getGuideAssigned().isEmpty()) {
            holder.tourStatus.setTextColor(context.getResources().getColor(R.color.teal_700));
        } else {
            holder.tourStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

        holder.cardView.setOnClickListener(v -> {
            if (onTourClickListener != null) {
                onTourClickListener.onTourClick(tour, position);
            } else {
                Intent intent = new Intent(context, TourDetailActivity.class);
                intent.putExtra("tour_id", tour.getId());
                intent.putExtra("tour_name", tour.getName());
                intent.putExtra("tour_description", tour.getDescription());
                intent.putExtra("tour_location", tour.getLocation());
                intent.putExtra("tour_price", tour.getPrice());
                intent.putExtra("tour_duration", tour.getDuration());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tourListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterPattern = constraint.toString().toLowerCase().trim();
                
                if (filterPattern.isEmpty()) {
                    tourListFiltered = new ArrayList<>(tourList);
                } else {
                    List<Tour> filteredList = new ArrayList<>();
                    for (Tour tour : tourList) {
                        if (tour.getLocation().toLowerCase().contains(filterPattern) ||
                            tour.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(tour);
                        }
                    }
                    tourListFiltered = filteredList;
                }
                
                FilterResults results = new FilterResults();
                results.values = tourListFiltered;
                results.count = tourListFiltered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                tourListFiltered = (List<Tour>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Método para filtrar por departamento específico
    public void filterByDepartment(String department) {
        if (department == null || department.isEmpty() || department.equals("todos")) {
            tourListFiltered = new ArrayList<>(tourList);
        } else {
            List<Tour> filteredList = new ArrayList<>();
            for (Tour tour : tourList) {
                if (tour.getLocation().toLowerCase().contains(department.toLowerCase())) {
                    filteredList.add(tour);
                }
            }
            tourListFiltered = filteredList;
        }
        notifyDataSetChanged();
    }

    public void updateTours(List<Tour> newTourList) {
        this.tourList = newTourList;
        this.tourListFiltered = new ArrayList<>(newTourList);
        notifyDataSetChanged();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView tourImage;
        TextView tourTitle;
        TextView tourDescription;
        TextView tourLocation;
        TextView tourPrice;
        TextView tourDuration;
        TextView tourStatus;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTour);
            tourImage = itemView.findViewById(R.id.tourImage);
            tourTitle = itemView.findViewById(R.id.tourTitle);
            tourDescription = itemView.findViewById(R.id.tourDescription);
            tourLocation = itemView.findViewById(R.id.tourLocation);
            tourPrice = itemView.findViewById(R.id.tourPrice);
            tourDuration = itemView.findViewById(R.id.tourDuration);
            tourStatus = itemView.findViewById(R.id.tourStatus);
        }
    }
}