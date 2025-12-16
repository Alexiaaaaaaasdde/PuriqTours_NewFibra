package com.example.puriqtours.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Company;

import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder> {

    private List<Company> companies;
    private Context context;

    public CompanyAdapter(Context context, List<Company> companies) {
        this.context = context;
        this.companies = companies;
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_company, parent, false);
        return new CompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        Company company = companies.get(position);

        holder.companyName.setText(company.getName());
        holder.companyRating.setRating((float) company.getRating());

        // 🔥 Cargar imagen desde URL (Firestore)
        Glide.with(context)
                .load(company.getImageUrl())
                .placeholder(R.drawable.kuelap)   // reemplaza por un placeholder real
                .into(holder.companyImage);
    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    public static class CompanyViewHolder extends RecyclerView.ViewHolder {
        ImageView companyImage;
        TextView companyName;
        RatingBar companyRating;

        public CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            companyImage = itemView.findViewById(R.id.ivCompany);
            companyName = itemView.findViewById(R.id.tvCompanyName);
            companyRating = itemView.findViewById(R.id.ratingBar);
        }
    }
}
