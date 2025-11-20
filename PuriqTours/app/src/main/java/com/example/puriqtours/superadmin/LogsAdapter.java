package com.example.puriqtours.superadmin;

import android.content.Context;
import android.text.TextUtils;
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

    private List<LogItem> logs;              // Lista filtrada
    private List<LogItem> logsOriginal;      // Lista original
    private Context context;

    public LogsAdapter(Context context, List<LogItem> logs) {
        this.context = context;
        this.logs = logs;
        this.logsOriginal = new ArrayList<>(logs); // ⭐ Guardamos copia para el buscador
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogItem log = logs.get(position);
        holder.tvTipo.setText(log.tipo);
        holder.tvFecha.setText(log.fecha);
        holder.tvDescripcion.setText(log.descripcion);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void setLogs(List<LogItem> nuevosLogs) {
        this.logs = nuevosLogs;
        this.logsOriginal = new ArrayList<>(nuevosLogs);
        notifyDataSetChanged();
    }

    // ⭐ FILTRO POR DESCRIPCIÓN (BUSCADOR)
    public void filter(String text) {
        logs.clear();

        if (TextUtils.isEmpty(text)) {
            logs.addAll(logsOriginal);  // ← restauramos la lista completa
        } else {
            String query = text.toLowerCase();

            for (LogItem log : logsOriginal) {
                if (log.descripcion != null &&
                        log.descripcion.toLowerCase().contains(query)) {
                    logs.add(log);
                }
            }
        }

        notifyDataSetChanged();
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvFecha, tvDescripcion;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        }
    }
}
