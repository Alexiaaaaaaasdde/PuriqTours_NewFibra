package com.example.puriqtours.login;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.puriqtours.R;
import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etLastName, etEmail, etBirthDate, etPhone, etAddress, etDocumentNumber;
    private Spinner spnDocumentType;
    private Button btnRegister;
    private ImageButton btnBack;

    private HashMap<String, String> userData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etBirthDate = findViewById(R.id.etBirthDate);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDocumentNumber = findViewById(R.id.etDocumentNumber);
        spnDocumentType = findViewById(R.id.spnDocumentType);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBackRegister);

        // Spinner tipo de documento
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"DNI", "Pasaporte", "Carnet Extranjería"});
        spnDocumentType.setAdapter(adapter);

        // Fecha
        etBirthDate.setOnClickListener(v -> showDatePicker());
        btnBack.setOnClickListener(v -> finish());

        // Continuar
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                userData.put("name", etName.getText().toString().trim());
                userData.put("last_name", etLastName.getText().toString().trim());
                userData.put("email", etEmail.getText().toString().trim());
                userData.put("birthdate", etBirthDate.getText().toString().trim());
                userData.put("phone", etPhone.getText().toString().trim());
                userData.put("address", etAddress.getText().toString().trim());
                userData.put("doc_type", spnDocumentType.getSelectedItem().toString());
                userData.put("document", etDocumentNumber.getText().toString().trim());
                userData.put("rol", "Cliente");
                userData.put("language", "es");

                Intent intent = new Intent(this, SetPasswordActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs() {
        if (etName.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty()) {
            Toast.makeText(this, "Completa al menos nombre y correo", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, y, m, d) ->
                etBirthDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }
}
