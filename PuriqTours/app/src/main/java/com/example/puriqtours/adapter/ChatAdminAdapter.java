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
import com.example.puriqtours.cliente.ChatActivity;
import com.example.puriqtours.entity.ChatAdmin;
import java.util.ArrayList;
import java.util.List;

public class ChatAdminAdapter extends RecyclerView.Adapter<ChatAdminAdapter.ChatViewHolder> implements Filterable {

    private Context context;
    private List<ChatAdmin> chatAdminList;
    private List<ChatAdmin> chatAdminListFiltered;
    private OnChatClickListener onChatClickListener;

    public interface OnChatClickListener {
        void onChatClick(ChatAdmin chatAdmin, int position);
        void onDeleteChat(ChatAdmin chatAdmin, int position);
    }

    public ChatAdminAdapter(Context context, List<ChatAdmin> chatAdminList) {
        this.context = context;
        this.chatAdminList = chatAdminList;
        this.chatAdminListFiltered = new ArrayList<>(chatAdminList);
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
        ChatAdmin chatAdmin = chatAdminListFiltered.get(position);
        
        holder.clientName.setText(chatAdmin.getClientName());
        holder.lastMessage.setText(chatAdmin.getLastMessage());
        holder.timeText.setText(chatAdmin.getTime());
        holder.tourName.setText("TourLegacy: " + chatAdmin.getTourName());
        
        // Configurar estado de mensajes no leídos
        if (chatAdmin.hasUnreadMessages()) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(chatAdmin.getUnreadCount()));
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
                onChatClickListener.onChatClick(chatAdmin, position);
            } else {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chat_id", chatAdmin.getId());
                intent.putExtra("client_name", chatAdmin.getClientName());
                intent.putExtra("tour_name", chatAdmin.getTourName());
                context.startActivity(intent);
            }
        });

        // Menú de tres puntos
        holder.menuButton.setOnClickListener(v -> showPopupMenu(v, chatAdmin, position));
    }

    private void showPopupMenu(View view, ChatAdmin chatAdmin, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.chat_item_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_chat) {
                if (onChatClickListener != null) {
                    onChatClickListener.onDeleteChat(chatAdmin, position);
                } else {
                    // Simulación de eliminación
                    Toast.makeText(context, "Chat con " + chatAdmin.getClientName() + " eliminado", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return chatAdminListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterPattern = constraint.toString().toLowerCase().trim();
                
                if (filterPattern.isEmpty()) {
                    chatAdminListFiltered = new ArrayList<>(chatAdminList);
                } else {
                    List<ChatAdmin> filteredList = new ArrayList<>();
                    for (ChatAdmin chatAdmin : chatAdminList) {
                        if (chatAdmin.getClientName().toLowerCase().contains(filterPattern) ||
                            chatAdmin.getTourName().toLowerCase().contains(filterPattern) ||
                            chatAdmin.getLastMessage().toLowerCase().contains(filterPattern)) {
                            filteredList.add(chatAdmin);
                        }
                    }
                    chatAdminListFiltered = filteredList;
                }
                
                FilterResults results = new FilterResults();
                results.values = chatAdminListFiltered;
                results.count = chatAdminListFiltered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                chatAdminListFiltered = (List<ChatAdmin>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void updateChats(List<ChatAdmin> newChatAdminList) {
        this.chatAdminList = newChatAdminList;
        this.chatAdminListFiltered = new ArrayList<>(newChatAdminList);
        notifyDataSetChanged();
    }

    public void removeChat(int position) {
        if (position >= 0 && position < chatAdminListFiltered.size()) {
            ChatAdmin removedChatAdmin = chatAdminListFiltered.get(position);
            chatAdminListFiltered.remove(position);
            chatAdminList.remove(removedChatAdmin);
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