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
import android.view.View;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class LogsActivity extends AppCompatActivity {
    private LogsAdapter logsAdapter;
    private RecyclerView rvLogs;
    private LogDbHelper dbHelper;

    // estado para exportar cuando se solicita permiso en < API 29
    private List<LogItem> pendingExportLogs;
    private String pendingExportFilename;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private String currentFilter = "General";

    private static final String CHANNEL_ID_DOWNLOADS = "downloads_channel";
    private static final int NOTIF_ID_DOWNLOAD = 1001;

    // en caso se pida permiso de notificaciones, guardamos la info a notificar
    private String pendingNotificationTitle;
    private String pendingNotificationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);

        // crear helper DB y datos iniciales
        dbHelper = new LogDbHelper(this);
        dbHelper.insertInitialIfEmpty();

        // Crear canal de notificaciones
        createNotificationChannel();

        // registrar launcher para permiso WRITE_EXTERNAL_STORAGE (solo usado en < Q)
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

        // registrar launcher para permiso de notificaciones (POST_NOTIFICATIONS)
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

        View vBtnReportesNav = findViewById(R.id.btnReportes);
        if (vBtnReportesNav != null) vBtnReportesNav.setOnClickListener(v -> {
            Intent intent = new Intent(LogsActivity.this, ReportesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        View vBtnLogsNav = findViewById(R.id.btnLogs);
        if (vBtnLogsNav != null) vBtnLogsNav.setOnClickListener(v -> {
            // ya estás en Logs
        });

        // RecyclerView y adapter (seguimos usando DB)
        rvLogs = findViewById(R.id.rvLogs);
        if (rvLogs != null) {
            rvLogs.setLayoutManager(new LinearLayoutManager(this));
            logsAdapter = new LogsAdapter(this, dbHelper.getLogsByTipo("General"));
            rvLogs.setAdapter(logsAdapter);
        }

        // Filtros: buscar cada botón con fallback (btnUsuariosFiltro o btnUsuarios) y asignar listeners si existen
        View btnGeneral = findViewById(R.id.btnGeneral);
        View btnUsuariosFilter = findViewById(R.id.btnUsuariosFiltro);
        View btnPagos = findViewById(R.id.btnPagos);
        View btnGuias = findViewById(R.id.btnGuias);
        View btnEmpresas = findViewById(R.id.btnEmpresas);

        if (btnGeneral != null) {
            btnGeneral.setOnClickListener(v -> {
                currentFilter = "General";
                if (logsAdapter != null) logsAdapter.setLogs(dbHelper.getLogsByTipo(currentFilter));
                highlightFilter(v.getId());
            });
        }
        if (btnUsuariosFilter != null) {
            btnUsuariosFilter.setOnClickListener(v -> {
                currentFilter = "Usuarios";
                if (logsAdapter != null) logsAdapter.setLogs(dbHelper.getLogsByTipo(currentFilter));
                highlightFilter(v.getId());
            });
        }
        if (btnPagos != null) {
            btnPagos.setOnClickListener(v -> {
                currentFilter = "Pagos";
                if (logsAdapter != null) logsAdapter.setLogs(dbHelper.getLogsByTipo(currentFilter));
                highlightFilter(v.getId());
            });
        }
        if (btnGuias != null) {
            btnGuias.setOnClickListener(v -> {
                currentFilter = "Guías";
                if (logsAdapter != null) logsAdapter.setLogs(dbHelper.getLogsByTipo(currentFilter));
                highlightFilter(v.getId());
            });
        }
        if (btnEmpresas != null) {
            btnEmpresas.setOnClickListener(v -> {
                currentFilter = "Empresas";
                if (logsAdapter != null) logsAdapter.setLogs(dbHelper.getLogsByTipo(currentFilter));
                highlightFilter(v.getId());
            });
        }

        // Botón Exportar: buscar de forma segura y asignar listener
        View btnExport = findViewById(R.id.btnExportLogs);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> {
                new AlertDialog.Builder(LogsActivity.this)
                    .setTitle("Exportar logs")
                    .setMessage("¿Guardar los logs filtrados en Descargas como PDF?")
                    .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<LogItem> toExport = dbHelper.getLogsByTipo(currentFilter);
                            String safeName = "logs_" + currentFilter.replaceAll("\\s+", "_").toLowerCase() + ".pdf";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                savePdfToDownloadsMediaStore(toExport, safeName);
                            } else {
                                if (ContextCompat.checkSelfPermission(LogsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    savePdfToDownloadsLegacy(toExport, safeName);
                                } else {
                                    pendingExportLogs = toExport;
                                    pendingExportFilename = safeName;
                                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                }
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            });
        }

        // Mostrar General por defecto
        if (btnGeneral != null) {
            highlightFilter(btnGeneral.getId());
        } else if (btnUsuariosFilter != null) {
            highlightFilter(btnUsuariosFilter.getId());
        } else {
            // fallback si no existen botones (no crash)
            highlightFilter(R.id.btnGeneral);
        }
    }

    // genera PDF en memoria y guarda mediante MediaStore (API >= 29)
    private void savePdfToDownloadsMediaStore(List<LogItem> logs, String filename) {
        PdfDocument pdfDocument = new PdfDocument();
        try {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // tamaño A4 aproximado en pts
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12f);
            int x = 20;
            int y = 40;
            // Header
            paint.setFakeBoldText(true);
            canvas.drawText("Logs - filtro: " + currentFilter, x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;
            canvas.drawText("Tipo | Fecha | Descripcion", x, y, paint);
            y += 18;
            // Filas
            for (LogItem li : logs) {
                String line = li.tipo + " | " + li.fecha + " | " + (li.descripcion != null ? li.descripcion : "");
                int maxCharsPerLine = 80;
                if (line.length() > maxCharsPerLine) {
                    String part = line.substring(0, Math.min(line.length(), maxCharsPerLine));
                    canvas.drawText(part, x, y, paint);
                    y += 16;
                    int from = maxCharsPerLine;
                    while (from < line.length()) {
                        String sub = line.substring(from, Math.min(line.length(), from + maxCharsPerLine));
                        canvas.drawText(sub, x, y, paint);
                        y += 16;
                        from += maxCharsPerLine;
                    }
                } else {
                    canvas.drawText(line, x, y, paint);
                    y += 16;
                }
                if (y > pageInfo.getPageHeight() - 40) {
                    pdfDocument.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }
            }
            pdfDocument.finishPage(page);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
            if (uri == null) {
                Toast.makeText(this, "No se pudo crear el archivo en Descargas", Toast.LENGTH_SHORT).show();
                return;
            }
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                pdfDocument.writeTo(os);
                Toast.makeText(this, "PDF guardado en Descargas: " + filename, Toast.LENGTH_SHORT).show();
                // notificar descarga
                notifyDownloadSaved(filename, "Descargas");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al escribir PDF", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generando PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    // guarda PDF en Downloads usando la ruta pública (API < 29). Requiere permiso WRITE_EXTERNAL_STORAGE.
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
            canvas.drawText("Logs - filtro: " + currentFilter, x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;
            canvas.drawText("Tipo | Fecha | Descripcion", x, y, paint);
            y += 18;
            for (LogItem li : logs) {
                String line = li.tipo + " | " + li.fecha + " | " + (li.descripcion != null ? li.descripcion : "");
                int maxCharsPerLine = 80;
                if (line.length() > maxCharsPerLine) {
                    String part = line.substring(0, Math.min(line.length(), maxCharsPerLine));
                    canvas.drawText(part, x, y, paint);
                    y += 16;
                    int from = maxCharsPerLine;
                    while (from < line.length()) {
                        String sub = line.substring(from, Math.min(line.length(), from + maxCharsPerLine));
                        canvas.drawText(sub, x, y, paint);
                        y += 16;
                        from += maxCharsPerLine;
                    }
                } else {
                    canvas.drawText(line, x, y, paint);
                    y += 16;
                }
                if (y > pageInfo.getPageHeight() - 40) {
                    pdfDocument.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }
            }
            pdfDocument.finishPage(page);

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();
            File outFile = new File(downloadsDir, filename);
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                pdfDocument.writeTo(fos);
                Toast.makeText(this, "PDF guardado en Descargas: " + outFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                // notificar descarga
                notifyDownloadSaved(filename, outFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al escribir PDF", Toast.LENGTH_SHORT).show();
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
                // guardar pendiente y pedir permiso
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
                int color = (id == selectedId) ? getResources().getColor(R.color.teal_50) : getResources().getColor(android.R.color.white);
                try {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                } catch (Exception ignored) {
                    btn.setBackgroundColor(color);
                }
            }
        }
    }
}


