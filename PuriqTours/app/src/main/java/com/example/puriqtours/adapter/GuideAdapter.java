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
import com.example.puriqtours.GuideDetailActivity;
import com.example.puriqtours.model.Guide;
import java.util.ArrayList;
import java.util.List;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> implements Filterable {

    private Context context;
    private List<Guide> guideList;
    private List<Guide> guideListFiltered;
    private OnGuideClickListener onGuideClickListener;

    public interface OnGuideClickListener {
        void onGuideClick(Guide guide, int position);
    }

    public GuideAdapter(Context context, List<Guide> guideList) {
        this.context = context;
        this.guideList = guideList;
        this.guideListFiltered = new ArrayList<>(guideList);
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
        Guide guide = guideListFiltered.get(position);
        
        holder.guideName.setText(guide.getName());
        holder.guideLocation.setText(guide.getLocation());
        
        if (guide.getImageResource() != 0) {
            holder.guideImage.setImageResource(guide.getImageResource());
        } else {
            holder.guideImage.setImageResource(R.drawable.avatar);
        }

        // Configurar rating (estrellas)
        int rating = guide.getRating();
        holder.star1.setTextColor(rating >= 1 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star2.setTextColor(rating >= 2 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star3.setTextColor(rating >= 3 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star4.setTextColor(rating >= 4 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));
        holder.star5.setTextColor(rating >= 5 ? context.getResources().getColor(R.color.yellow) : context.getResources().getColor(R.color.gray_light));

        // Estado de disponibilidad
        if (guide.isAvailable()) {
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
                onGuideClickListener.onGuideClick(guide, position);
            } else {
                Intent intent = new Intent(context, GuideDetailActivity.class);
                intent.putExtra("guide_id", guide.getId());
                intent.putExtra("guide_name", guide.getName());
                intent.putExtra("guide_location", guide.getLocation());
                intent.putExtra("guide_rating", guide.getRating());
                intent.putExtra("guide_available", guide.isAvailable());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return guideListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterPattern = constraint.toString().toLowerCase().trim();
                
                if (filterPattern.isEmpty()) {
                    guideListFiltered = new ArrayList<>(guideList);
                } else {
                    List<Guide> filteredList = new ArrayList<>();
                    for (Guide guide : guideList) {
                        if (guide.getLocation().toLowerCase().contains(filterPattern) ||
                            guide.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(guide);
                        }
                    }
                    guideListFiltered = filteredList;
                }
                
                FilterResults results = new FilterResults();
                results.values = guideListFiltered;
                results.count = guideListFiltered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                guideListFiltered = (List<Guide>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Método para filtrar por departamento específico
    public void filterByDepartment(String department) {
        if (department == null || department.isEmpty() || department.equals("todos")) {
            guideListFiltered = new ArrayList<>(guideList);
        } else {
            List<Guide> filteredList = new ArrayList<>();
            for (Guide guide : guideList) {
                if (guide.getLocation().toLowerCase().contains(department.toLowerCase())) {
                    filteredList.add(guide);
                }
            }
            guideListFiltered = filteredList;
        }
        notifyDataSetChanged();
    }

    public void updateGuides(List<Guide> newGuideList) {
        this.guideList = newGuideList;
        this.guideListFiltered = new ArrayList<>(newGuideList);
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