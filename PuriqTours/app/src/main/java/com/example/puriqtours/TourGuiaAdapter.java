package com.example.puriqtours;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private FragmentManager fragmentManager;
    private List<Tour> tours;

    public TourAdapter(List<Tour> tours, FragmentManager fragmentManager) {
        this.tours = tours;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(vista);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tours.get(position);

        holder.tvTitulo.setText(tour.getTitulo());
        holder.tvDescripcionCorta.setText(tour.getDescripcion());
        holder.imgSolicitud.setImageResource(tour.getImagenResId());
        holder.tvDescripcionCompleta.setText(tour.getDescripcion());
        holder.tvCiudad.setText("Ciudad: " + tour.getCiudad());
        holder.tvFecha.setText("Fecha: " + tour.getFecha());
        holder.tvRangoHora.setText("Hora: " + tour.getHoraInicio() + "-" + tour.getHoraFin());

        boolean expandido = tour.isExpandido();
        holder.layoutExpandible.setVisibility(expandido ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            tour.setExpandido(!tour.isExpandido());
            notifyItemChanged(position);
        });

        holder.btnDetalles.setOnClickListener(v -> {
            DetallesBottomSheet bottomSheet = new DetallesBottomSheet();
            bottomSheet.show(fragmentManager, bottomSheet.getTag());
        });

        holder.btnIniciar.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), IniciarTourActivity.class);
            v.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSolicitud;
        TextView tvTitulo, tvDescripcionCorta, tvDescripcionCompleta, tvCiudad, tvFecha, tvRangoHora;
        LinearLayout layoutExpandible;
        Button btnDetalles, btnIniciar;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSolicitud = itemView.findViewById(R.id.imgSolicitud);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcionCorta = itemView.findViewById(R.id.tvDescripcionCorta);
            tvDescripcionCompleta = itemView.findViewById(R.id.tvDescripcionCompleta);
            tvCiudad = itemView.findViewById(R.id.tvCiudad);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvRangoHora = itemView.findViewById(R.id.tvRangoHora);
            layoutExpandible = itemView.findViewById(R.id.layoutExpandible);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            btnIniciar = itemView.findViewById(R.id.btnIniciar);

        }
    }
}
