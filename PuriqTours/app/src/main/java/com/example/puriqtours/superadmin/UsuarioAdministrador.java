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
import java.util.List;public class UsuarioAdministrador extends Usuario {
    public String empresa;
    public String fechaRegistro;
    public UsuarioAdministrador(String nombre, String ciudad, String empresa, String fechaRegistro) {
        super(nombre, ciudad);
        this.empresa = empresa;
        this.fechaRegistro = fechaRegistro;
    }
    @Override
    public int getTipo() { return 2; }
}
