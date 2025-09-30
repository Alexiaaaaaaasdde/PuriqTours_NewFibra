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
public class GuiasHorizontalAdapter extends RecyclerView.Adapter<GuiasHorizontalAdapter.GuiaViewHolder> {
    private List<UsuarioGuia> guias;
    private Context context;
    public GuiasHorizontalAdapter(Context context, List<UsuarioGuia> guias) {
        this.context = context;
        this.guias = guias;
    }

    @NonNull
    @Override
    public GuiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guia_horizontal, parent, false);
        return new GuiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuiaViewHolder holder, int position) {
        UsuarioGuia guia = guias.get(position);
        holder.txtGuiaNombre.setText(guia.nombre);
        holder.imgGuiaAvatar.setImageResource(R.drawable.avatar1);
    }

    @Override
    public int getItemCount() {
        return guias.size();
    }

    public static class GuiaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGuiaAvatar;
        TextView txtGuiaNombre;
        public GuiaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGuiaAvatar = itemView.findViewById(R.id.imgGuiaAvatar);
            txtGuiaNombre = itemView.findViewById(R.id.txtGuiaNombre);
        }
    }
}
