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
    private ArrayList<TextInputLayout> listaCampos = new ArrayList<>();

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

        // ---------- SPINNER DE ROLES ----------
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.roles_usuarios,
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerRol.setAdapter(adapter);

        spinnerRol.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                dibujarCampos();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // ---------- BOTÓN CREAR ----------
        MaterialButton btnCrear = view.findViewById(R.id.btnCrear);
        btnCrear.setOnClickListener(v -> crearUsuario());

        // ---------- CREAR DIÁLOGO ----------
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

// ⭐ HACER EL DIÁLOGO ANCHO
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        return dialog;

    }

    // ==========================
    // DIBUJAR CAMPOS DINÁMICOS
    // ==========================

    private void dibujarCampos() {
        contenedorCampos.removeAllViews();
        listaCampos.clear();

        String rol = spinnerRol.getSelectedItem().toString();

        agregarCampo("Nombre", InputType.TYPE_CLASS_TEXT);
        agregarCampo("Apellido", InputType.TYPE_CLASS_TEXT);
        agregarCampo("Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        agregarCampo("Password", InputType.TYPE_TEXT_VARIATION_PASSWORD);

        if (rol.equals("Cliente") || rol.equals("Guia")) {
            agregarCampo("Teléfono", InputType.TYPE_CLASS_PHONE);
            agregarCampo("Dirección", InputType.TYPE_CLASS_TEXT);
            agregarCampoSelector("Idioma", new String[]{"Español", "Inglés", "Aymara"});
        }

        if (rol.equals("Cliente")) {
            agregarCampoSelector("Tipo documento", new String[]{"DNI", "Pasaporte", "Carnet extranjería"});
            agregarCampo("Número documento", InputType.TYPE_CLASS_NUMBER);
            agregarCampoFecha("Fecha nacimiento");
        }

        if (rol.equals("Admin")) {
            agregarCampoSelector("Idioma", new String[]{"Español", "Inglés", "Aymara"});
        }
    }

    // ---------- Campo normal ----------
    private void agregarCampo(String hint, int tipo) {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint(hint);

        TextInputEditText et = new TextInputEditText(requireContext());
        et.setInputType(tipo);
        et.setPadding(0, 30, 0, 30);

        til.addView(et);
        contenedorCampos.addView(til);
        listaCampos.add(til);
    }

    // ---------- Campo fecha con calendario ----------
    private void agregarCampoFecha(String hint) {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint(hint);

        TextInputEditText et = new TextInputEditText(requireContext());
        et.setFocusable(false);
        et.setClickable(true);
        et.setPadding(0, 30, 0, 30);

        et.setOnClickListener(v -> abrirSelectorFecha(et));

        til.addView(et);
        contenedorCampos.addView(til);
        listaCampos.add(til);
    }

    private void abrirSelectorFecha(TextInputEditText et) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, y, m, d) -> et.setText(d + "/" + (m+1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    // ---------- Campo selector (Spinner) ----------
    private void agregarCampoSelector(String hint, String[] valores) {

        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint(hint);

        Spinner spinner = new Spinner(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                valores
        );
        spinner.setAdapter(adapter);

        til.addView(spinner);
        contenedorCampos.addView(til);
        listaCampos.add(til);
    }

    // ==========================
    // CREAR USUARIO
    // ==========================

    private void crearUsuario() {

        Map<String, Object> data = new HashMap<>();
        String rol = spinnerRol.getSelectedItem().toString();
        if (rol.equals("Guía")) rol = "Guia";
        if (rol.equals("Administrador")) rol = "Admin";
        int index = 0;

        String nombre = getCampoTexto(index++);
        String apellido = getCampoTexto(index++);
        String email = getCampoTexto(index++);
        String password = getCampoTexto(index++);

        data.put("name", nombre);
        data.put("last_name", apellido);
        data.put("email", email);
        data.put("rol", rol);
        data.put("status", "Activo");

        if (rol.equals("Cliente") || rol.equals("Guia")) {
            data.put("phone", getCampoTexto(index++));
            data.put("address", getCampoTexto(index++));
            data.put("language", getCampoSpinner(index++));
        }

        if (rol.equals("Cliente")) {
            data.put("doc_type", getCampoSpinner(index++));
            data.put("document", getCampoTexto(index++));
            data.put("birthdate", getCampoTexto(index++));
        }

        if (rol.equals("Admin")) {
            data.put("language", getCampoSpinner(index++));
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    db.collection("users").document(uid)
                            .set(data)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(getContext(),
                                        "Usuario creado correctamente",
                                        Toast.LENGTH_LONG).show();
                                dismiss();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // ---------- Helpers ----------
    private String getCampoTexto(int index) {
        TextInputLayout til = listaCampos.get(index);
        return ((TextInputEditText) til.getEditText()).getText().toString();
    }

    private String getCampoSpinner(int index) {
        TextInputLayout til = listaCampos.get(index);
        Spinner sp = (Spinner) til.getChildAt(0);
        return sp.getSelectedItem().toString();
    }
}
