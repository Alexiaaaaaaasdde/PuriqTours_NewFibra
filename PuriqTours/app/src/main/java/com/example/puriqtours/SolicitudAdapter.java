package com.example.puriqtours;

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

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    private FragmentManager fragmentManager;
    private List<Solicitud> solicitudes;

    public SolicitudAdapter(List<Solicitud> solicitudes, FragmentManager fragmentManager) {
        this.solicitudes = solicitudes;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new SolicitudViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        Solicitud solicitud = solicitudes.get(position);

        holder.tvTitulo.setText(solicitud.getTitulo());
        holder.tvDescripcionCorta.setText(solicitud.getDescripcion());
        holder.imgSolicitud.setImageResource(solicitud.getImagenResId());
        holder.tvDescripcionCompleta.setText(solicitud.getDescripcion());

        boolean expandido = solicitud.isExpandido();
        holder.layoutExpandible.setVisibility(expandido ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            solicitud.setExpandido(!solicitud.isExpandido());
            notifyItemChanged(position);
        });

        holder.btnDetalles.setOnClickListener(v -> {
            DetallesBottomSheet bottomSheet = new DetallesBottomSheet();
            bottomSheet.show(fragmentManager, bottomSheet.getTag());
        });

        holder.btnIniciar.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Rechazaste: " + solicitud.getTitulo(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSolicitud;
        TextView tvTitulo, tvDescripcionCorta, tvDescripcionCompleta;
        LinearLayout layoutExpandible;
        Button btnDetalles, btnIniciar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSolicitud = itemView.findViewById(R.id.imgSolicitud);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcionCorta = itemView.findViewById(R.id.tvDescripcionCorta);
            tvDescripcionCompleta = itemView.findViewById(R.id.tvDescripcionCompleta);
            layoutExpandible = itemView.findViewById(R.id.layoutExpandible);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            btnIniciar = itemView.findViewById(R.id.btnIniciar);

        }
    }
}
