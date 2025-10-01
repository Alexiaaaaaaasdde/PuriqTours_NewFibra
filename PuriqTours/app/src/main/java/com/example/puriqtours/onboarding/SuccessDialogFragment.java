package com.example.puriqtours.onboarding;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.puriqtours.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SuccessDialogFragment extends DialogFragment {

    public interface OnClosed { void onClosed(); }
    private OnClosed listener;

    private static final String TAG = "SuccessDialogFragment";

    public SuccessDialogFragment setOnClosed(OnClosed l) {
        this.listener = l;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Diálogo creado");

        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_success, null, false);

        v.findViewById(R.id.btnClose).setOnClickListener(x -> {
            Log.d(TAG, "Botón cerrar presionado");
            dismiss();
        });

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setCancelable(false)
                .create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "Diálogo descartado, ejecutando listener");

        // 🔥 Asegurar que el listener se ejecute
        if (listener != null) {
            listener.onClosed();
        } else {
            Log.e(TAG, "Listener es null!");
        }
    }
}