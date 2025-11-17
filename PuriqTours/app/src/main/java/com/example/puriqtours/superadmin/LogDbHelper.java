package com.example.puriqtours.superadmin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LogDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "puriq_logs.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_LOGS = "logs";
    public static final String COL_ID = "_id";
    public static final String COL_TIPO = "tipo";
    public static final String COL_FECHA = "fecha";
    public static final String COL_DESCRIPCION = "descripcion";

    public LogDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_LOGS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TIPO + " TEXT, " +
                COL_FECHA + " TEXT, " +
                COL_DESCRIPCION + " TEXT" +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // sencillo: eliminar y crear de nuevo (solo en desarrollo)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

    public long insertLog(String tipo, String fecha, String descripcion) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TIPO, tipo);
        cv.put(COL_FECHA, fecha);
        cv.put(COL_DESCRIPCION, descripcion);
        long id = db.insert(TABLE_LOGS, null, cv);
        return id;
    }

    public List<LogItem> getLogsByTipo(String tipo) {
        List<LogItem> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String[] selectionArgs;
        if (tipo == null || tipo.isEmpty() || tipo.equalsIgnoreCase("General")) {
            // General: devolver todos los logs (según tu criterio). Aquí devolvemos todos.
            selection = null;
            selectionArgs = null;
        } else {
            selection = COL_TIPO + " = ?";
            selectionArgs = new String[]{ tipo };
        }
        Cursor c = db.query(TABLE_LOGS, null, selection, selectionArgs, null, null, COL_ID + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                String t = c.getString(c.getColumnIndexOrThrow(COL_TIPO));
                String f = c.getString(c.getColumnIndexOrThrow(COL_FECHA));
                String d = c.getString(c.getColumnIndexOrThrow(COL_DESCRIPCION));
                lista.add(new LogItem(t, f, d));
            }
            c.close();
        }
        return lista;
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LOGS, null);
        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
        }
        return count;
    }

    // Inserta 20 registros de ejemplo si la tabla está vacía
    public void insertInitialIfEmpty() {
        if (getCount() > 0) return;
        String[] tipos = {"Empresas", "Guias", "Pagos", "Usuarios", "General"};
        // Fecha ejemplo fija similar a lo que ya usas
        for (int i = 1; i <= 20; i++) {
            String tipo = tipos[(i - 1) % tipos.length];
            String fecha = "04/09/2025 14:" + String.format("%02d", i) + ":00";
            String descripcion = "El " + tipo.toLowerCase() + " " + tipo + i + " realizó una acción de ejemplo.";
            insertLog(tipo, fecha, descripcion);
        }
    }
}
