package com.example.puriqtours.superadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Map;

public class TopToursAdapter extends RecyclerView.Adapter<TopToursAdapter.ViewHolder> {

    private List<Tour> lista;
    private Map<String, Integer> conteo;
    private int maxValor = 1;

    public TopToursAdapter(List<Tour> lista, Map<String, Integer> conteo) {
        this.lista = lista;
        this.conteo = conteo;

        for (Tour t : lista) {
            int v = conteo.get(t.getIdTour());
            if (v > maxValor) maxValor = v;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tour_top, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Tour tour = lista.get(pos);
        int reservas = conteo.get(tour.getIdTour());

        h.tvTourName.setText(tour.getTitle());
        h.tvReservas.setText(String.valueOf(reservas));

        float porcentaje = (float) reservas / maxValor * 100f;
        h.progress.setProgress((int) porcentaje);

        // 🔹 Importa arriba en tu archivo:
// import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
// import com.bumptech.glide.request.RequestOptions;

        int radius = 24; // Radio de las esquinas (en píxeles, puedes probar 16, 20 o 24)
        Glide.with(h.itemView.getContext())
                .load(tour.getImageUrl())
                .apply(new RequestOptions()
                        .centerCrop()  // mantiene la proporción
                        .transform(new RoundedCorners(radius))
                        .placeholder(R.drawable.placeholder_gray))
                .into(h.imgTour);

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTourName, tvReservas;
        ShapeableImageView imgTour;
        ProgressBar progress;

        ViewHolder(View item) {
            super(item);
            tvTourName = item.findViewById(R.id.tvTourName);
            tvReservas = item.findViewById(R.id.tvReservas);
            imgTour = item.findViewById(R.id.imgTour);
            progress = item.findViewById(R.id.progressBar);
        }
    }
}
