package com.example.puriqtours.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener clickListener;
    private OnNotificationDeleteListener deleteListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public interface OnNotificationDeleteListener {
        void onNotificationDelete(Notification notification);
    }

    public NotificationAdapter(Context context, 
                             OnNotificationClickListener clickListener,
                             OnNotificationDeleteListener deleteListener) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        holder.tvTourName.setText(notification.getTourName());
        holder.tvGuideName.setText("Guía: " + notification.getGuideName());
        holder.tvClientCount.setText(notification.getClientCount() + " clientes finalizaron");
        
        // Formatear fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(notification.getTimestamp()));
        holder.tvTimestamp.setText(formattedDate);
        
        // Indicador de leído/no leído
        if (notification.isRead()) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.viewIndicator.setVisibility(View.GONE);
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.notification_unread_bg));
            holder.viewIndicator.setVisibility(View.VISIBLE);
        }
        
        // Click en la notificación
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNotificationClick(notification);
            }
        });
        
        // Click en eliminar
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onNotificationDelete(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateData(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View viewIndicator;
        TextView tvTourName, tvGuideName, tvClientCount, tvTimestamp;
        ImageButton btnDelete;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardNotification);
            viewIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
            tvTourName = itemView.findViewById(R.id.tvNotificationTourName);
            tvGuideName = itemView.findViewById(R.id.tvNotificationGuideName);
            tvClientCount = itemView.findViewById(R.id.tvNotificationClientCount);
            tvTimestamp = itemView.findViewById(R.id.tvNotificationTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotification);
        }
    }
}
