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
import com.example.puriqtours.admin.GuideDetailActivity;
import com.example.puriqtours.entity.GuideAdmin;
import java.util.ArrayList;
import java.util.List;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> implements Filterable {

    private Context context;
    private List<GuideAdmin> guideAdminList;
    private List<GuideAdmin> guideAdminListFiltered;
    private OnGuideClickListener onGuideClickListener;

    public interface OnGuideClickListener {
        void onGuideClick(GuideAdmin guideAdmin, int position);
    }

    public GuideAdapter(Context context, List<GuideAdmin> guideAdminList) {
        this.context = context;
        this.guideAdminList = guideAdminList;
        this.guideAdminListFiltered = new ArrayList<>(guideAdminList);
    }

    public void setOnGuideClickListener(OnGuideClickListener listener) {
        this.onGuideClickListener = listener;
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guide, parent, false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        GuideAdmin guideAdmin = guideAdminListFiltered.get(position);
        
        holder.guideName.setText(guideAdmin.getName());
        holder.guideLocation.setText(guideAdmin.getLocation());
        
        if (guideAdmin.getImageResource() != 0) {
            holder.guideImage.setImageResource(guideAdmin.getImageResource());
        } else {
            holder.guideImage.setImageResource(R.drawable.avatar);
        }

        // Configurar rating (estrellas)
        int rating = guideAdmin.getRating();
        holder.star1.setTextColor(rating >= 1 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star2.setTextColor(rating >= 2 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star3.setTextColor(rating >= 3 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star4.setTextColor(rating >= 4 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star5.setTextColor(rating >= 5 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));

        // Estado de disponibilidad
        if (guideAdmin.isAvailable()) {
            holder.availabilityDot.setBackgroundResource(R.drawable.circle_green);
            holder.availabilityText.setText("Disponible para un tour");
            holder.availabilityText.setTextColor(context.getResources().getColor(R.color.teal_700));
        } else {
            holder.availabilityDot.setBackgroundResource(R.drawable.circle_gray);
            holder.availabilityText.setText("No disponible");
            holder.availabilityText.setTextColor(context.getResources().getColor(R.color.gray));
        }

        holder.cardView.setOnClickListener(v -> {
            if (onGuideClickListener != null) {
                onGuideClickListener.onGuideClick(guideAdmin, position);
            } else {
                Intent intent = new Intent(context, GuideDetailActivity.class);
                intent.putExtra("guide_id", guideAdmin.getId());
                intent.putExtra("guide_name", guideAdmin.getName());
                intent.putExtra("guide_location", guideAdmin.getLocation());
                intent.putExtra("guide_rating", guideAdmin.getRating());
                intent.putExtra("guide_available", guideAdmin.isAvailable());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return guideAdminListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterPattern = constraint.toString().toLowerCase().trim();
                
                if (filterPattern.isEmpty()) {
                    guideAdminListFiltered = new ArrayList<>(guideAdminList);
                } else {
                    List<GuideAdmin> filteredList = new ArrayList<>();
                    for (GuideAdmin guideAdmin : guideAdminList) {
                        if (guideAdmin.getLocation().toLowerCase().contains(filterPattern) ||
                            guideAdmin.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(guideAdmin);
                        }
                    }
                    guideAdminListFiltered = filteredList;
                }
                
                FilterResults results = new FilterResults();
                results.values = guideAdminListFiltered;
                results.count = guideAdminListFiltered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                guideAdminListFiltered = (List<GuideAdmin>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Método para filtrar por departamento específico
    public void filterByDepartment(String department) {
        if (department == null || department.isEmpty() || department.equals("todos")) {
            guideAdminListFiltered = new ArrayList<>(guideAdminList);
        } else {
            List<GuideAdmin> filteredList = new ArrayList<>();
            for (GuideAdmin guideAdmin : guideAdminList) {
                if (guideAdmin.getLocation().toLowerCase().contains(department.toLowerCase())) {
                    filteredList.add(guideAdmin);
                }
            }
            guideAdminListFiltered = filteredList;
        }
        notifyDataSetChanged();
    }

    public void updateGuides(List<GuideAdmin> newGuideAdminList) {
        this.guideAdminList = newGuideAdminList;
        this.guideAdminListFiltered = new ArrayList<>(newGuideAdminList);
        notifyDataSetChanged();
    }

    public static class GuideViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView guideImage;
        TextView guideName;
        TextView guideLocation;
        TextView star1, star2, star3, star4, star5;
        View availabilityDot;
        TextView availabilityText;

        public GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardGuide);
            guideImage = itemView.findViewById(R.id.guideImage);
            guideName = itemView.findViewById(R.id.guideName);
            guideLocation = itemView.findViewById(R.id.guideLocation);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
            availabilityDot = itemView.findViewById(R.id.availabilityDot);
            availabilityText = itemView.findViewById(R.id.availabilityText);
        }
    }
}