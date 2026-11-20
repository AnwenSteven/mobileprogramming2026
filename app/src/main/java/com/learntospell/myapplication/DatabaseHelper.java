package com.learntospell.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "vocab_game.db";
    private static final int DATABASE_VERSION = 1;

    // Konstanta Tabel Sesi
    public static final String TABLE_SESI = "tabel_sesi";
    public static final String COL_SESI_ID = "id";
    public static final String COL_SESI_LEVEL = "level";
    public static final String COL_SESI_BENAR = "benar";
    public static final String COL_SESI_SALAH = "salah";

    // Konstanta Tabel Salah
    public static final String TABLE_SALAH = "tabel_salah";
    public static final String COL_SALAH_ID = "id";
    public static final String COL_SALAH_SESI_ID = "sesi_id";
    public static final String COL_SALAH_DETAIL_KATA = "detail_kata";

    private static final String SQL_CREATE_TABLE_SESI =
            "CREATE TABLE " + TABLE_SESI + " (" +
                    COL_SESI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_SESI_LEVEL + " TEXT, " +
                    COL_SESI_BENAR + " INTEGER, " +
                    COL_SESI_SALAH + " INTEGER)";

    private static final String SQL_CREATE_TABLE_SALAH =
            "CREATE TABLE " + TABLE_SALAH + " (" +
                    COL_SALAH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_SALAH_SESI_ID + " INTEGER, " +
                    COL_SALAH_DETAIL_KATA + " TEXT, " +
                    "FOREIGN KEY (" + COL_SALAH_SESI_ID + ") REFERENCES " +
                    TABLE_SESI + "(" + COL_SESI_ID + ") ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_SESI);
        db.execSQL(SQL_CREATE_TABLE_SALAH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALAH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESI);
        onCreate(db);
    }

    public long simpanSesi(String level, int benar, int salah, ArrayList<String> listSalah) {
        SQLiteDatabase db = this.getWritableDatabase();
        long sesiId = -1;

        db.beginTransaction();
        try {
            ContentValues valuesSesi = new ContentValues();
            valuesSesi.put(COL_SESI_LEVEL, level);
            valuesSesi.put(COL_SESI_BENAR, benar);
            valuesSesi.put(COL_SESI_SALAH, salah);

            sesiId = db.insert(TABLE_SESI, null, valuesSesi);

            if (sesiId != -1 && listSalah != null) {
                for (String detailKata : listSalah) {
                    ContentValues valuesSalah = new ContentValues();
                    valuesSalah.put(COL_SALAH_SESI_ID, sesiId);
                    valuesSalah.put(COL_SALAH_DETAIL_KATA, detailKata);
                    db.insert(TABLE_SALAH, null, valuesSalah);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }

        return sesiId;
    }
    public void hapusSemuaRiwayat() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_SALAH, null, null);
            db.delete(TABLE_SESI, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


}
