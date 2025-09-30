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
public abstract class Usuario {
    public String nombre;
    public String ciudad;
    public Usuario(String nombre, String ciudad) {
        this.nombre = nombre;
        this.ciudad = ciudad;
    }
    public abstract int getTipo(); // 0=Cliente, 1=Guía, 2=Administrador
}
