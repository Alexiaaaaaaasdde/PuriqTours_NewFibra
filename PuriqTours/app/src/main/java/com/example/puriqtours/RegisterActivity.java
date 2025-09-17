package com.example.puriqtours;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    EditText etBirthDate;
    Spinner spnDocumentType;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // === FECHA DE NACIMIENTO ===
        etBirthDate = findViewById(R.id.etBirthDate);

        etBirthDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, y, m, d) -> etBirthDate.setText(d + "/" + (m + 1) + "/" + y),
                    year, month, day
            );
            datePicker.show();
        });

        // === SPINNER DE DOCUMENTO ===
        spnDocumentType = findViewById(R.id.spnDocumentType);

        // Lista de documentos
        String[] docTypes = {
                "Selecciona tipo de documento",
                "DNI",
                "Carnet de extranjería",
                "Pasaporte"
        };

        // Adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                docTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar al Spinner
        spnDocumentType.setAdapter(adapter);

        // === BOTÓN REGISTRAR ===
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            if (spnDocumentType.getSelectedItem().toString().equals("Selecciona tipo de documento")) {
                Toast.makeText(this, "Por favor selecciona un tipo de documento", Toast.LENGTH_SHORT).show();
            } else {
                // Continuar con registro
                Toast.makeText(this, "Documento: " + spnDocumentType.getSelectedItem(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
