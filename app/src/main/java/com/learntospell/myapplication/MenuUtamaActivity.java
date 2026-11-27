package com.learntospell.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MenuUtamaActivity extends AppCompatActivity {

    private EditText etNamaPlayer;
    private Button btnMulai, btnSettings, btnHistory, btnReset;
    private DatabaseHelper dbHelper;

    // Nilai default untuk Pengaturan Game
    private int jumlahSoal = 10;
    private int detikPerSoal = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_utama);

        // Inisialisasi DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Inisialisasi View
        etNamaPlayer = findViewById(R.id.etNamaPlayer);
        btnMulai = findViewById(R.id.btnMulai);
        btnSettings = findViewById(R.id.btnSettings);
        btnHistory = findViewById(R.id.btnHistory);

        // 1. Tombol Mulai Game (Validasi nama & kirim data via Intent)
        btnMulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String namaPlayer = etNamaPlayer.getText().toString().trim();

                if (TextUtils.isEmpty(namaPlayer)) {
                    etNamaPlayer.setError("Nama player wajib diisi!");
                    Toast.makeText(MenuUtamaActivity.this, "Masukkan nama terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MenuUtamaActivity.this, MainActivity.class);
                intent.putExtra("NAMA_PLAYER", namaPlayer);
                intent.putExtra("JUMLAH_SOAL", jumlahSoal);
                intent.putExtra("DETIK_PER_SOAL", detikPerSoal);
                startActivity(intent);
            }
        });

        // 2. Tombol Settings (Pop-up AlertDialog pengubah jumlah soal & durasi)
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilkanDialogSettings();
            }
        });

        // 3. Tombol History (Navigasi ke HistoryActivity)
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuUtamaActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // 4. Tombol Reset (Pop-up AlertDialog konfirmasi hapus riwayat)
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilkanDialogReset();
            }
        });
    }

    // Method untuk menampilkan AlertDialog Pengaturan
    private void tampilkanDialogSettings() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        TextView labelSoal = new TextView(this);
        labelSoal.setText("Jumlah Soal");
        layout.addView(labelSoal);

        final EditText inputSoal = new EditText(this);
        inputSoal.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputSoal.setText(String.valueOf(jumlahSoal));
        layout.addView(inputSoal);

        TextView labelDetik = new TextView(this);
        labelDetik.setText("Detik Per Soal");
        labelDetik.setPadding(0, 30, 0, 0);
        layout.addView(labelDetik);

        final EditText inputDetik = new EditText(this);
        inputDetik.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputDetik.setText(String.valueOf(detikPerSoal));
        layout.addView(inputDetik);

        new AlertDialog.Builder(this)
                .setTitle("Pengaturan Game")
                .setView(layout)
                .setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            int soal = Integer.parseInt(inputSoal.getText().toString().trim());
                            int detik = Integer.parseInt(inputDetik.getText().toString().trim());

                            if (soal > 0 && detik > 0) {
                                jumlahSoal = soal;
                                detikPerSoal = detik;
                                Toast.makeText(MenuUtamaActivity.this, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MenuUtamaActivity.this, "Nilai harus lebih besar dari 0!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(MenuUtamaActivity.this, "Input tidak valid!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // Method untuk menampilkan AlertDialog Konfirmasi Reset
    private void tampilkanDialogReset() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Reset")
                .setMessage("Apakah Anda yakin ingin menghapus seluruh riwayat permainan?")
                .setPositiveButton("Ya, Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.hapusSemuaRiwayat();
                        Toast.makeText(MenuUtamaActivity.this, "Seluruh riwayat berhasil dihapus!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
