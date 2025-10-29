package com.example.puriqtours.superadmin;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.puriqtours.R;
import java.util.List;
public class AvatarNombreAdapter extends RecyclerView.Adapter<AvatarNombreAdapter.ViewHolder> {
    private List<String> nombres;
    private Context context;
    private int avatarResId;
    public AvatarNombreAdapter(Context context, List<String> nombres, int avatarResId) {
        this.context = context;
        this.nombres = nombres;
        this.avatarResId = avatarResId;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avatar_nombre, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtNombre.setText(nombres.get(position));
        holder.imgAvatar.setImageResource(avatarResId);
    }
    @Override
    public int getItemCount() {
        return nombres.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtNombre;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtNombre = itemView.findViewById(R.id.txtNombre);
        }
    }
}
