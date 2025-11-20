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
import com.example.puriqtours.admin.TourDetailActivity;
import com.example.puriqtours.entity.TourAdmin;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> implements Filterable {

    private Context context;
    private List<TourAdmin> tourAdminList;
    private List<TourAdmin> tourAdminListFiltered;
    private OnTourClickListener onTourClickListener;

    public interface OnTourClickListener {
        void onTourClick(TourAdmin tourAdmin, int position);
    }

    public TourAdapter(Context context, List<TourAdmin> tourAdminList) {
        this.context = context;
        this.tourAdminList = tourAdminList;
        this.tourAdminListFiltered = new ArrayList<>(tourAdminList);
    }

    public void setOnTourClickListener(OnTourClickListener listener) {
        this.onTourClickListener = listener;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tour_admin, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        TourAdmin tourAdmin = tourAdminListFiltered.get(position);

        holder.tourTitle.setText(tourAdmin.getName());
        holder.tourDescription.setText(tourAdmin.getDescription());
        holder.tourLocation.setText(tourAdmin.getRegion());
        holder.tourPrice.setText("S/ " + tourAdmin.getPrice());
        holder.tourDuration.setText(tourAdmin.getDurationText());

        // Cargar imagen con Picasso desde Firebase Storage
        if (tourAdmin.getImageUrl() != null && !tourAdmin.getImageUrl().isEmpty()) {
            Picasso.get()
                .load(tourAdmin.getImageUrl())
                .placeholder(R.drawable.kuelap)
                .error(R.drawable.kuelap)
                .into(holder.tourImage);
        } else {
            holder.tourImage.setImageResource(R.drawable.kuelap);
        }

        holder.cardView.setOnClickListener(v -> {
            if (onTourClickListener != null) {
                onTourClickListener.onTourClick(tourAdmin, position);
            } else {
                Intent intent = new Intent(context, TourDetailActivity.class);
                intent.putExtra("tour_id", tourAdmin.getId());
                intent.putExtra("tour_name", tourAdmin.getName());
                intent.putExtra("tour_description", tourAdmin.getDescription());
                intent.putExtra("tour_location", tourAdmin.getLocation());
                intent.putExtra("tour_price", tourAdmin.getPrice());
                intent.putExtra("tour_duration", tourAdmin.getDuration());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tourAdminListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterPattern = constraint.toString().toLowerCase().trim();

                if (filterPattern.isEmpty()) {
                    tourAdminListFiltered = new ArrayList<>(tourAdminList);
                } else {
                    List<TourAdmin> filteredList = new ArrayList<>();
                    for (TourAdmin tourAdmin : tourAdminList) {
                        // Buscar por nombre, ubicación o región
                        if (tourAdmin.getName().toLowerCase().contains(filterPattern) ||
                            tourAdmin.getLocation().toLowerCase().contains(filterPattern) ||
                            (tourAdmin.getRegion() != null && tourAdmin.getRegion().toLowerCase().contains(filterPattern))) {
                            filteredList.add(tourAdmin);
                        }
                    }
                    tourAdminListFiltered = filteredList;
                }

                FilterResults results = new FilterResults();
                results.values = tourAdminListFiltered;
                results.count = tourAdminListFiltered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                tourAdminListFiltered = (List<TourAdmin>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Método para filtrar por región/departamento específico
    public void filterByDepartment(String department) {
        if (department == null || department.isEmpty() || department.equals("todos")) {
            tourAdminListFiltered = new ArrayList<>(tourAdminList);
        } else {
            List<TourAdmin> filteredList = new ArrayList<>();
            for (TourAdmin tourAdmin : tourAdminList) {
                // Filtrar por región (departamento)
                if (tourAdmin.getRegion() != null && 
                    tourAdmin.getRegion().toLowerCase().contains(department.toLowerCase())) {
                    filteredList.add(tourAdmin);
                }
            }
            tourAdminListFiltered = filteredList;
        }
        notifyDataSetChanged();
    }

    public void updateTours(List<TourAdmin> newTourAdminList) {
        this.tourAdminList = newTourAdminList;
        this.tourAdminListFiltered = new ArrayList<>(newTourAdminList);
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

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTour);
            tourImage = itemView.findViewById(R.id.tourImage);
            tourTitle = itemView.findViewById(R.id.tourTitle);
            tourDescription = itemView.findViewById(R.id.tourDescription);
            tourLocation = itemView.findViewById(R.id.tourLocation);
            tourPrice = itemView.findViewById(R.id.tourPrice);
            tourDuration = itemView.findViewById(R.id.tourDuration);
        }
    }
}
