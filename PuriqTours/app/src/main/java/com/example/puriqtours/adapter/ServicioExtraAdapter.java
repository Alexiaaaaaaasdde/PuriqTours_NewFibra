package com.example.puriqtours.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour.ServicioExtra;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicioExtraAdapter extends RecyclerView.Adapter<ServicioExtraAdapter.ViewHolder> {

    private List<ServicioExtra> serviciosExtras;
    private Map<String, Integer> cantidadesSeleccionadas;

    // ✅ CONSTRUCTOR ACTUALIZADO - Recibe el Map existente
    public ServicioExtraAdapter(List<ServicioExtra> serviciosExtras, Map<String, Integer> cantidadesExistentes) {
        this.serviciosExtras = serviciosExtras;

        // ✅ Si hay cantidades previas, usarlas; si no, crear nuevo Map
        if (cantidadesExistentes != null) {
            this.cantidadesSeleccionadas = new HashMap<>(cantidadesExistentes);
        } else {
            this.cantidadesSeleccionadas = new HashMap<>();
        }
    }

    // ✅ AGREGAR MÉTODO PARA OBTENER EL MAP
    public Map<String, Integer> getCantidadesSeleccionadas() {
        return cantidadesSeleccionadas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_extra_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServicioExtra extra = serviciosExtras.get(position);
        holder.bind(extra);
    }

    @Override
    public int getItemCount() {
        return serviciosExtras != null ? serviciosExtras.size() : 0;
    }

    public List<ServicioExtra> getExtrasSeleccionados() {
        List<ServicioExtra> seleccionados = new ArrayList<>();
        for (ServicioExtra extra : serviciosExtras) {
            Integer cantidad = cantidadesSeleccionadas.get(extra.getTitle());
            if (cantidad != null && cantidad > 0) {
                for (int i = 0; i < cantidad; i++) {
                    seleccionados.add(extra);
                }
            }
        }
        return seleccionados;
    }

    public float getPrecioTotalExtras() {
        float total = 0;
        for (ServicioExtra extra : serviciosExtras) {
            Integer cantidad = cantidadesSeleccionadas.get(extra.getTitle());
            if (cantidad != null && cantidad > 0) {
                total += extra.getPrice() * cantidad;
            }
        }
        return total;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExtraTitulo, tvExtraPrecio, tvCantidad;
        ImageView imgExtraServicio;
        ImageButton btnMenos, btnMas;
        ServicioExtra currentExtra;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExtraTitulo = itemView.findViewById(R.id.tvExtraTitulo);
            tvExtraPrecio = itemView.findViewById(R.id.tvExtraPrecio);
            imgExtraServicio = itemView.findViewById(R.id.imgExtraServicio);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            btnMenos = itemView.findViewById(R.id.btnMenos);
            btnMas = itemView.findViewById(R.id.btnMas);

            btnMas.setOnClickListener(v -> {
                int cantidad = getCantidadActual();
                setCantidad(cantidad + 1);
            });

            btnMenos.setOnClickListener(v -> {
                int cantidad = getCantidadActual();
                if (cantidad > 0) {
                    setCantidad(cantidad - 1);
                }
            });
        }

        public void bind(ServicioExtra extra) {
            this.currentExtra = extra;

            tvExtraTitulo.setText(extra.getTitle());
            double precio = extra.getPrice() != null ? extra.getPrice() : 0.0;
            tvExtraPrecio.setText("S/ " + String.format("%.2f", precio));

            if (extra.getImageUrl() != null && !extra.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(extra.getImageUrl())
                        .placeholder(R.drawable.kuelap)
                        .into(imgExtraServicio);
            } else {
                imgExtraServicio.setImageResource(R.drawable.kuelap);
            }

            // ✅ MOSTRAR LA CANTIDAD ACTUAL (puede ser > 0 si ya se había seleccionado antes)
            int cantidad = getCantidadActual();
            tvCantidad.setText(String.valueOf(cantidad));
        }

        private int getCantidadActual() {
            Integer cantidad = cantidadesSeleccionadas.get(currentExtra.getTitle());
            return cantidad != null ? cantidad : 0;
        }

        private void setCantidad(int cantidad) {
            cantidadesSeleccionadas.put(currentExtra.getTitle(), cantidad);
            tvCantidad.setText(String.valueOf(cantidad));
        }
    }
}
