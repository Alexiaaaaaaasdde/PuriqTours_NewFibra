package com.example.puriqtours.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.ChatThread;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter
        extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatThread chat);
    }

    private final List<ChatThread> chats;
    private final List<ChatThread> chatsOriginal;
    private final OnChatClickListener listener;

    public ChatListAdapter(List<ChatThread> chats,
                           OnChatClickListener listener) {
        this.chats = chats;
        this.chatsOriginal = new ArrayList<>(chats);
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

        holder.lastMessage.setText(
                chat.getLastMessage() == null || chat.getLastMessage().isEmpty()
                        ? "Sin mensajes aún"
                        : chat.getLastMessage()
        );

        holder.timeText.setText(formatearHora(chat.getLastTimestamp()));

        // Avatar por defecto
        holder.clientAvatar.setImageResource(R.drawable.avatar);

        // Ocultar badge (no implementado aún)
        holder.unreadBadge.setVisibility(View.GONE);
        holder.unreadCount.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    // 🔎 BÚSQUEDA
    public void filtrar(String texto) {
        chats.clear();

        if (texto == null || texto.trim().isEmpty()) {
            chats.addAll(chatsOriginal);
        } else {
            String filtro = texto.toLowerCase();
            for (ChatThread chat : chatsOriginal) {
                if ((chat.getClientName() != null &&
                        chat.getClientName().toLowerCase().contains(filtro)) ||
                        (chat.getTourName() != null &&
                                chat.getTourName().toLowerCase().contains(filtro))) {
                    chats.add(chat);
                }
            }
        }
        notifyDataSetChanged();
    }

    private String formatearHora(Timestamp ts) {
        if (ts == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(ts.toDate());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView clientAvatar;
        View unreadBadge;

        TextView clientName, tourName, lastMessage, timeText, unreadCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            clientAvatar = itemView.findViewById(R.id.clientAvatar);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            unreadCount = itemView.findViewById(R.id.unreadCount);
            clientName = itemView.findViewById(R.id.clientName);
            tourName = itemView.findViewById(R.id.tourName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}
