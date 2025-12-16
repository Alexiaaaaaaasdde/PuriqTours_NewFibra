package com.example.puriqtours.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.ChatThread;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatThreadAdapter
        extends RecyclerView.Adapter<ChatThreadAdapter.ViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatThread chat);
    }

    private final List<ChatThread> chats;
    private final OnChatClickListener listener;

    public ChatThreadAdapter(List<ChatThread> chats, OnChatClickListener listener) {
        this.chats = chats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatThread chat = chats.get(position);

        holder.clientName.setText(chat.getClientName());
        holder.tourName.setText(chat.getTourName());

        if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
            holder.lastMessage.setText(chat.getLastMessage());
        } else {
            holder.lastMessage.setText("Sin mensajes aún");
        }

        holder.timeText.setText(formatearHora(chat.getLastTimestamp()));

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    // 🕒 Formatear hora
    private String formatearHora(Timestamp ts) {
        if (ts == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(ts.toDate());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView clientName, tourName, lastMessage, timeText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            clientName = itemView.findViewById(R.id.clientName);
            tourName = itemView.findViewById(R.id.tourName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}