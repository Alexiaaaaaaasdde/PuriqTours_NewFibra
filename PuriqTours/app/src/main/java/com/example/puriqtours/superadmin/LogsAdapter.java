package com.example.puriqtours.superadmin;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;

import java.util.List;
public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {
    private List<LogItem> logs;
    private Context context;
public LogsAdapter(Context context, List<LogItem> logs) {
        this.context = context;
        this.logs = logs;
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
 public void setLogs(List<LogItem> logs) {
        this.logs = logs;
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
