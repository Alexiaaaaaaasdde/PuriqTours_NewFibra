package com.example.puriqtours.superadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;

import java.util.ArrayList;
import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private Context context;
    private List<LogItem> logs;

    public LogsAdapter(Context context, List<LogItem> logs) {
        this.context = context;
        this.logs = logs != null ? logs : new ArrayList<>();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogItem log = logs.get(position);

        // 🔹 Tipo
        holder.tvTipo.setText(log.getType() != null ? log.getType() : "GENERAL");

        // 🔹 Descripción
        holder.tvDescripcion.setText(
                log.getDesc() != null ? log.getDesc() : "Sin descripción"
        );

        // 🔹 Fecha formateada
        holder.tvFecha.setText(log.getFechaFormateada());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    // =====================================================
    // 🔄 ACTUALIZAR LISTA (para filtros)
    // =====================================================
    public void setLogs(List<LogItem> nuevaLista) {
        this.logs = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    // =====================================================
    // 🧩 VIEW HOLDER
    // =====================================================
    static class LogViewHolder extends RecyclerView.ViewHolder {

        TextView tvTipo, tvDescripcion, tvFecha;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTipo = itemView.findViewById(R.id.tvLogType);
            tvDescripcion = itemView.findViewById(R.id.tvLogDesc);
            tvFecha = itemView.findViewById(R.id.tvLogTime);
        }
    }
}
