package com.learntospell.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "vocab_game.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_SESI = "tabel_sesi";
    public static final String COL_SESI_ID = "id";
    public static final String COL_SESI_NAMA_PLAYER = "nama_player";
    public static final String COL_SESI_LEVEL = "level";
    public static final String COL_SESI_BENAR = "benar";
    public static final String COL_SESI_SALAH = "salah";
    public static final String COL_SESI_SKOR = "skor";
    public static final String COL_SESI_CATATAN_WAKTU = "catatan_waktu";
    public static final String TABLE_DETAIL = "tabel_detail";
    public static final String COL_DETAIL_ID = "id";
    public static final String COL_DETAIL_SESI_ID = "sesi_id";
    public static final String COL_DETAIL_KATA = "kata";
    public static final String COL_DETAIL_JAWABAN_USER = "jawaban_user";
    public static final String COL_DETAIL_STATUS = "status";

    // Query pembuatan tabel_sesi
    private static final String SQL_CREATE_TABLE_SESI =
            "CREATE TABLE " + TABLE_SESI + " (" +
                    COL_SESI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_SESI_NAMA_PLAYER + " TEXT, " +
                    COL_SESI_LEVEL + " TEXT, " +
                    COL_SESI_BENAR + " INTEGER, " +
                    COL_SESI_SALAH + " INTEGER, " +
                    COL_SESI_SKOR + " INTEGER, " +
                    COL_SESI_CATATAN_WAKTU + " TEXT)";

    // Query pembuatan tabel_detail d
    private static final String SQL_CREATE_TABLE_DETAIL =
            "CREATE TABLE " + TABLE_DETAIL + " (" +
                    COL_DETAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DETAIL_SESI_ID + " INTEGER, " +
                    COL_DETAIL_KATA + " TEXT, " +
                    COL_DETAIL_JAWABAN_USER + " TEXT, " +
                    COL_DETAIL_STATUS + " TEXT, " +
                    "FOREIGN KEY (" + COL_DETAIL_SESI_ID + ") REFERENCES " +
                    TABLE_SESI + "(" + COL_SESI_ID + ") ON DELETE CASCADE)";

    public static class DetailJawaban {
        public String kata;
        public String jawabanUser;
        public String status;

        public DetailJawaban(String kata, String jawabanUser, String status) {
            this.kata = kata;
            this.jawabanUser = jawabanUser;
            this.status = status;
        }
    }

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
        db.execSQL(SQL_CREATE_TABLE_DETAIL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS tabel_salah");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESI);
        onCreate(db);
    }

    public long simpanSesi(String namaPlayer, String level, int benar, int salah, int skor, String catatanWaktu, ArrayList<DetailJawaban> listDetail) {
        SQLiteDatabase db = this.getWritableDatabase();
        long sesiId = -1;

        db.beginTransaction();
        try {
            ContentValues valuesSesi = new ContentValues();
            valuesSesi.put(COL_SESI_NAMA_PLAYER, namaPlayer);
            valuesSesi.put(COL_SESI_LEVEL, level);
            valuesSesi.put(COL_SESI_BENAR, benar);
            valuesSesi.put(COL_SESI_SALAH, salah);
            valuesSesi.put(COL_SESI_SKOR, skor);
            valuesSesi.put(COL_SESI_CATATAN_WAKTU, catatanWaktu);

            sesiId = db.insert(TABLE_SESI, null, valuesSesi);

            if (sesiId != -1 && listDetail != null) {
                for (DetailJawaban detail : listDetail) {
                    ContentValues valuesDetail = new ContentValues();
                    valuesDetail.put(COL_DETAIL_SESI_ID, sesiId);
                    valuesDetail.put(COL_DETAIL_KATA, detail.kata);
                    valuesDetail.put(COL_DETAIL_JAWABAN_USER, detail.jawabanUser);
                    valuesDetail.put(COL_DETAIL_STATUS, detail.status);
                    db.insert(TABLE_DETAIL, null, valuesDetail);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return sesiId;
    }


    public Cursor ambilSemuaSesi() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_SESI,
                null,
                null,
                null,
                null,
                null,
                COL_SESI_ID + " DESC"
        );
    }

    public Cursor ambilDetailSesi(long sesiId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_DETAIL,
                null,
                COL_DETAIL_SESI_ID + " = ?",
                new String[]{String.valueOf(sesiId)},
                null,
                null,
                COL_DETAIL_ID + " ASC"
        );
    }
    public void hapusSemuaRiwayat() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_DETAIL, null, null);
            db.delete(TABLE_SESI, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


}
