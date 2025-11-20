package com.example.puriqtours.superadmin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogsActivity extends AppCompatActivity {
    private LogsAdapter logsAdapter;
    private RecyclerView rvLogs;
    private FirebaseFirestore db;
    
    private List<LogItem> allLogs = new ArrayList<>();
    private List<LogItem> filteredLogs = new ArrayList<>();
    
    // Estado
    private String currentFilter = "general";
    private boolean sortAscending = true;
    private String searchQuery = "";

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    
    private List<LogItem> pendingExportLogs;
    private String pendingExportFilename;
    private String pendingNotificationTitle;
    private String pendingNotificationMessage;

    private static final String CHANNEL_ID_DOWNLOADS = "downloads_channel";
    private static final int NOTIF_ID_DOWNLOAD = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Crear canal de notificaciones
        createNotificationChannel();

        // registrar launcher para permiso WRITE_EXTERNAL_STORAGE
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted && pendingExportLogs != null && pendingExportFilename != null) {
                    savePdfToDownloadsLegacy(pendingExportLogs, pendingExportFilename);
                } else if (!isGranted) {
                    Toast.makeText(this, "Permiso denegado: no se puede guardar en Descargas", Toast.LENGTH_SHORT).show();
                }
                pendingExportLogs = null;
                pendingExportFilename = null;
            }
        );

        // registrar launcher para permiso de notificaciones
        notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    if (pendingNotificationTitle != null && pendingNotificationMessage != null) {
                        showDownloadNotification(pendingNotificationTitle, pendingNotificationMessage);
                    }
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                }
                pendingNotificationTitle = null;
                pendingNotificationMessage = null;
            }
        );

        // BottomBar navegación universal: listeners seguros
        View vBtnPrincipal = findViewById(R.id.btnPrincipal);
        if (vBtnPrincipal != null) vBtnPrincipal.setOnClickListener(v -> {
            Intent intent = new Intent(LogsActivity.this, MainSuperAdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        View vBtnUsuariosNav = findViewById(R.id.btnUsuarios);
        if (vBtnUsuariosNav != null) vBtnUsuariosNav.setOnClickListener(v -> {
            Intent intent = new Intent(LogsActivity.this, UsuariosActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });



        View vBtnLogsNav = findViewById(R.id.btnLogs);
        if (vBtnLogsNav != null) vBtnLogsNav.setOnClickListener(v -> {
            // ya estás en Logs
        });

        // RecyclerView y adapter
        rvLogs = findViewById(R.id.rvLogs);
        if (rvLogs != null) {
            rvLogs.setLayoutManager(new LinearLayoutManager(this));
            logsAdapter = new LogsAdapter(this, filteredLogs);
            rvLogs.setAdapter(logsAdapter);
        }

        // SearchView para buscar por descripción
        EditText etSearch = findViewById(R.id.etSearchLogs);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString().toLowerCase();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Botón Ordenar
        View btnSort = findViewById(R.id.btnOrdenarLogs);
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> {
                sortAscending = !sortAscending;
                applyFilters();
                String mensaje = sortAscending ? "Más antiguo → Más reciente" : "Más reciente → Más antiguo";
                Toast.makeText(LogsActivity.this, mensaje, Toast.LENGTH_SHORT).show();
            });
        }

        // Filtros por tipo
        View btnGeneral = findViewById(R.id.btnGeneral);
        View btnUsuariosFilter = findViewById(R.id.btnUsuariosFiltro);
        View btnPagos = findViewById(R.id.btnPagos);
        View btnGuias = findViewById(R.id.btnGuias);
        View btnEmpresas = findViewById(R.id.btnEmpresas);

        if (btnGeneral != null) {
            btnGeneral.setOnClickListener(v -> {
                currentFilter = "general";
                loadLogsFromFirestore();
                highlightFilter(v.getId());
            });
        }
        if (btnUsuariosFilter != null) {
            btnUsuariosFilter.setOnClickListener(v -> {
                currentFilter = "usuarios";
                loadLogsFromFirestore();
                highlightFilter(v.getId());
            });
        }
        if (btnPagos != null) {
            btnPagos.setOnClickListener(v -> {
                currentFilter = "pagos";
                loadLogsFromFirestore();
                highlightFilter(v.getId());
            });
        }
        if (btnGuias != null) {
            btnGuias.setOnClickListener(v -> {
                currentFilter = "guias";
                loadLogsFromFirestore();
                highlightFilter(v.getId());
            });
        }
        if (btnEmpresas != null) {
            btnEmpresas.setOnClickListener(v -> {
                currentFilter = "empresas";
                loadLogsFromFirestore();
                highlightFilter(v.getId());
            });
        }

        // Botón Exportar
        View btnExport = findViewById(R.id.btnExportLogs);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> {
                new AlertDialog.Builder(LogsActivity.this)
                    .setTitle("Exportar logs")
                    .setMessage("¿Guardar los logs filtrados en Descargas como PDF?")
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String safeName = "logs_" + currentFilter.replaceAll("\\s+", "_").toLowerCase() + ".pdf";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            savePdfToDownloadsMediaStore(filteredLogs, safeName);
                        } else {
                            if (ContextCompat.checkSelfPermission(LogsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                savePdfToDownloadsLegacy(filteredLogs, safeName);
                            } else {
                                pendingExportLogs = filteredLogs;
                                pendingExportFilename = safeName;
                                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            });
        }

        // Cargar logs inicialmente (General)
        loadLogsFromFirestore();
        
        // Mostrar General por defecto
        if (btnGeneral != null) {
            highlightFilter(btnGeneral.getId());
        } else if (btnUsuariosFilter != null) {
            highlightFilter(btnUsuariosFilter.getId());
        }
    }

    /**
     * Carga los logs desde Firestore según el filtro actual
     */
    private void loadLogsFromFirestore() {
        android.util.Log.d("LogsActivity", "Cargando logs - Filtro: " + currentFilter);
        
        // Obtener todos los logs (sin filtro ni order by para evitar índice)
        db.collection("logs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allLogs.clear();
                List<LogItem> tempLogs = new ArrayList<>();
                
                for (DocumentSnapshot doc : task.getResult()) {
                    try {
                        String tipo = doc.getString("type");
                        String descripcion = doc.getString("desc");
                        Object timestampObj = doc.get("timestamp");
                        String fecha = "";
                        long timestampMiliseconds = 0;
                        
                        if (timestampObj != null) {
                            com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) timestampObj;
                            Date date = ts.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                            fecha = sdf.format(date);
                            timestampMiliseconds = date.getTime();
                        }
                        
                        LogItem log = new LogItem(tipo, fecha, descripcion);
                        log.timestamp = timestampMiliseconds;
                        tempLogs.add(log);
                    } catch (Exception e) {
                        android.util.Log.e("LogsActivity", "Error procesando log: " + e.getMessage());
                    }
                }
                
                // Ordenar por timestamp descendente (más reciente primero)
                Collections.sort(tempLogs, (a, b) -> Long.compare(b.timestamp, a.timestamp));
                
                // Filtrar por tipo actual
                for (LogItem log : tempLogs) {
                    if (log.tipo.equalsIgnoreCase(currentFilter)) {
                        allLogs.add(log);
                    }
                }
                
                applyFilters();
                android.util.Log.d("LogsActivity", "Logs cargados: " + allLogs.size());
            } else {
                android.util.Log.e("LogsActivity", "Error cargando logs: " + task.getException());
                Toast.makeText(LogsActivity.this, "Error cargando logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Aplica filtros de búsqueda y ordenamiento a los logs
     */
    private void applyFilters() {
        filteredLogs.clear();
        
        // Filtrar por búsqueda (descripción)
        for (LogItem log : allLogs) {
            if (searchQuery.isEmpty() || log.descripcion.toLowerCase().contains(searchQuery)) {
                filteredLogs.add(log);
            }
        }
        
        // Ordenar por timestamp
        if (sortAscending) {
            // Ascendente: más antiguo primero
            Collections.reverse(filteredLogs);
        }
        // else: descendente (más reciente primero) - ya viene así de Firestore
        
        if (logsAdapter != null) {
            logsAdapter.setLogs(filteredLogs);
        }
    }

    // genera PDF en memoria y guarda mediante MediaStore (API >= 29)
    private void savePdfToDownloadsMediaStore(List<LogItem> logs, String filename) {
        PdfDocument pdfDocument = new PdfDocument();
        try {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 en pts
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12f);
            int x = 20;
            int y = 40;
            
            // Header
            paint.setFakeBoldText(true);
            canvas.drawText("Logs - Filtro: " + currentFilter, x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;
            canvas.drawText("Tipo | Fecha | Descripción", x, y, paint);
            y += 18;
            
            // Filas
            for (LogItem log : logs) {
                String line = log.tipo + " | " + log.fecha + " | " + (log.descripcion != null ? log.descripcion : "");
                if (y > 800) {
                    pdfDocument.finishPage(page);
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }
                canvas.drawText(line, x, y, paint);
                y += 16;
            }
            pdfDocument.finishPage(page);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
            if (uri == null) {
                Toast.makeText(this, "Error: no se pudo crear el archivo", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                pdfDocument.writeTo(os);
                notifyDownloadSaved(filename, "Descargas");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error escribiendo PDF", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generando PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    // guarda PDF en Downloads usando la ruta pública (API < 29)
    private void savePdfToDownloadsLegacy(List<LogItem> logs, String filename) {
        PdfDocument pdfDocument = new PdfDocument();
        try {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12f);
            int x = 20;
            int y = 40;
            
            paint.setFakeBoldText(true);
            canvas.drawText("Logs - Filtro: " + currentFilter, x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;
            canvas.drawText("Tipo | Fecha | Descripción", x, y, paint);
            y += 18;
            
            for (LogItem log : logs) {
                String line = log.tipo + " | " + log.fecha + " | " + (log.descripcion != null ? log.descripcion : "");
                if (y > 800) {
                    pdfDocument.finishPage(page);
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }
                canvas.drawText(line, x, y, paint);
                y += 16;
            }
            pdfDocument.finishPage(page);

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();
            
            File outFile = new File(downloadsDir, filename);
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                pdfDocument.writeTo(fos);
                notifyDownloadSaved(filename, "Descargas");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error escribiendo PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generando PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    // Decide si puede notificar ahora o debe pedir permiso (Android 13+)
    private void notifyDownloadSaved(String filename, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                showDownloadNotification("Descarga completada", filename + " guardado en " + message);
            } else {
                pendingNotificationTitle = "Descarga completada";
                pendingNotificationMessage = filename + " guardado en " + message;
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            showDownloadNotification("Descarga completada", filename + " guardado en " + message);
        }
    }

    @SuppressLint("MissingPermission")
    private void showDownloadNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_DOWNLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(NOTIF_ID_DOWNLOAD, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Descargas";
            String description = "Notificaciones de archivos descargados";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_DOWNLOADS, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void highlightFilter(int selectedId) {
        int[] candidateIds = {R.id.btnGeneral, R.id.btnUsuariosFiltro, R.id.btnUsuarios, R.id.btnPagos, R.id.btnGuias, R.id.btnEmpresas};
        for (int id : candidateIds) {
            View btn = findViewById(id);
            if (btn != null) {
                if (id == selectedId) {
                    btn.setAlpha(1.0f);
                    btn.setScaleX(1.1f);
                    btn.setScaleY(1.1f);
                } else {
                    btn.setAlpha(0.5f);
                    btn.setScaleX(1.0f);
                    btn.setScaleY(1.0f);
                }
            }
        }
    }
}



