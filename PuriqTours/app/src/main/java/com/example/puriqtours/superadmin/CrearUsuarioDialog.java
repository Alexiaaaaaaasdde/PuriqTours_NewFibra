package com.example.puriqtours.superadmin;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.puriqtours.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CrearUsuarioDialog extends AppCompatDialogFragment {

    private Spinner spinnerRol;
    private LinearLayout contenedorCampos;

    // Listas separadas
    private ArrayList<TextInputLayout> listaCamposTexto = new ArrayList<>();
    private ArrayList<Spinner> listaSpinners = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_crear_usuario, null);

        spinnerRol = view.findViewById(R.id.spinnerRol);
        contenedorCampos = view.findViewById(R.id.contenedorCampos);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Administrador"}
        );
        spinnerRol.setAdapter(adapter);


        spinnerRol.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                dibujarCampos();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        MaterialButton btnCrear = view.findViewById(R.id.btnCrear);
        btnCrear.setOnClickListener(v -> crearUsuario());

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        return dialog;
    }

    // =====================================================
    // DIBUJAR CAMPOS
    // =====================================================

    private void dibujarCampos() {
        contenedorCampos.removeAllViews();
        listaCamposTexto.clear();
        listaSpinners.clear();

        String rol = spinnerRol.getSelectedItem().toString();

        agregarCampoTexto("Nombre");
        agregarCampoTexto("Apellido");
        agregarCampoTexto("Email");
        agregarCampoTexto("Password", InputType.TYPE_TEXT_VARIATION_PASSWORD);


        if (rol.equals("Administrador") || rol.equals("Admin")) {
            agregarCampoSpinner("Idioma", new String[]{"Español", "Inglés", "Aymara"});
        }
    }

    // =====================================================
    // CAMPOS
    // =====================================================

    private void agregarCampoTexto(String hint) {
        agregarCampoTexto(hint, InputType.TYPE_CLASS_TEXT);
    }

    private void agregarCampoTexto(String hint, int tipo) {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint(hint);

        TextInputEditText et = new TextInputEditText(requireContext());
        et.setInputType(tipo);
        et.setPadding(0, 30, 0, 30);

        til.addView(et);
        contenedorCampos.addView(til);

        listaCamposTexto.add(til);
    }

    private void agregarCampoSpinner(String hint, String[] valores) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 20, 0, 20);

        TextView label = new TextView(requireContext());
        label.setText(hint);
        label.setTextSize(16);
        label.setPadding(0, 10, 0, 10);

        Spinner spinner = new Spinner(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                valores
        );
        spinner.setAdapter(adapter);

        layout.addView(label);
        layout.addView(spinner);
        contenedorCampos.addView(layout);

        listaSpinners.add(spinner);
    }

    // =====================================================
    // CREAR USUARIO
    // =====================================================

    private void crearUsuario() {

        String rol = "Admin";

        int i = 0;

        String nombre = getTexto(i++);
        String apellido = getTexto(i++);
        String email = getTexto(i++);
        String password = getTexto(i++);

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "❗Completa todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", nombre);
        data.put("last_name", apellido);
        data.put("email", email);
        data.put("rol", rol);
        data.put("status", "Activo");
// ✅ Guardar idioma del administrador
        String idioma = listaSpinners.get(0).getSelectedItem().toString();
        data.put("language", idioma);




        Toast.makeText(getContext(), "Creando usuario...", Toast.LENGTH_SHORT).show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();
                    data.put("uid", uid);

                    db.collection("users").document(uid)
                            .set(data)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(getContext(), "✅ Usuario creado correctamente", Toast.LENGTH_LONG).show();
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "❌ Error Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error Auth: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String getTexto(int index) {
        return listaCamposTexto.get(index).getEditText().getText().toString();
    }
}
