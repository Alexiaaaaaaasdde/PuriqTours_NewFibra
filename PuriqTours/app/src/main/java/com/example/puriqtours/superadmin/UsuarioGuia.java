package com.example.puriqtours.superadmin;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.puriqtours.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
public class UsuarioGuia extends Usuario {
    public int valoracion;
    public UsuarioGuia(String nombre, String ciudad, int valoracion) {
        super(nombre, ciudad);
        this.valoracion = valoracion;
    }
    @Override
    public int getTipo() { return 1; }
}
