package com.example.puriqtours.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.puriqtours.R;
import com.example.puriqtours.ChatActivity;
import com.example.puriqtours.model.Chat;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> implements Filterable {

    private Context context;
    private List<Chat> chatList;
    private List<Chat> chatListFiltered;
    private OnChatClickListener onChatClickListener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat, int position);
        void onDeleteChat(Chat chat, int position);
    }

    public ChatAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.chatListFiltered = new ArrayList<>(chatList);
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.onChatClickListener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatListFiltered.get(position);
        
        holder.clientName.setText(chat.getClientName());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.timeText.setText(chat.getTime());
        holder.tourName.setText("Tour: " + chat.getTourName());
        
        // Configurar estado de mensajes no leídos
        if (chat.hasUnreadMessages()) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(chat.getUnreadCount()));
            holder.clientName.setTextColor(context.getResources().getColor(R.color.black));
            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
            holder.clientName.setTextColor(context.getResources().getColor(R.color.gray));
            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.gray));
        }

        // Click en la card para abrir chat
        holder.cardView.setOnClickListener(v -> {
            if (onChatClickListener != null) {
                onChatClickListener.onChatClick(chat, position);
            } else {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chat_id", chat.getId());
                intent.putExtra("client_name", chat.getClientName());
                intent.putExtra("tour_name", chat.getTourName());
                context.startActivity(intent);
            }
        });

        // Menú de tres puntos
        holder.menuButton.setOnClickListener(v -> showPopupMenu(v, chat, position));
    }

    private void showPopupMenu(View view, Chat chat, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.chat_item_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_chat) {
                if (onChatClickListener != null) {
                    onChatClickListener.onDeleteChat(chat, position);
                } else {
                    // Simulación de eliminación
                    Toast.makeText(context, "Chat con " + chat.getClientName() + " eliminado", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return chatListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterPattern = constraint.toString().toLowerCase().trim();
                
                if (filterPattern.isEmpty()) {
                    chatListFiltered = new ArrayList<>(chatList);
                } else {
                    List<Chat> filteredList = new ArrayList<>();
                    for (Chat chat : chatList) {
                        if (chat.getClientName().toLowerCase().contains(filterPattern) ||
                            chat.getTourName().toLowerCase().contains(filterPattern) ||
                            chat.getLastMessage().toLowerCase().contains(filterPattern)) {
                            filteredList.add(chat);
                        }
                    }
                    chatListFiltered = filteredList;
                }
                
                FilterResults results = new FilterResults();
                results.values = chatListFiltered;
                results.count = chatListFiltered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                chatListFiltered = (List<Chat>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void updateChats(List<Chat> newChatList) {
        this.chatList = newChatList;
        this.chatListFiltered = new ArrayList<>(newChatList);
        notifyDataSetChanged();
    }

    public void removeChat(int position) {
        if (position >= 0 && position < chatListFiltered.size()) {
            Chat removedChat = chatListFiltered.get(position);
            chatListFiltered.remove(position);
            chatList.remove(removedChat);
            notifyItemRemoved(position);
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView clientName;
        TextView lastMessage;
        TextView timeText;
        TextView tourName;
        ImageView menuButton;
        View unreadBadge;
        TextView unreadCount;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardChat);
            clientName = itemView.findViewById(R.id.clientName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timeText = itemView.findViewById(R.id.timeText);
            tourName = itemView.findViewById(R.id.tourName);
            menuButton = itemView.findViewById(R.id.menuButton);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            unreadCount = itemView.findViewById(R.id.unreadCount);
        }
    }
}