package com.learntospell.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HistoryActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;
    private RecyclerView rvHistory;
    private TextView textKosong;
    private Button buttonHapusRiwayat, buttonKembali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        textKosong = findViewById(R.id.textkosong);
        buttonHapusRiwayat = findViewById(R.id.buttonHapusRiwayat);
        buttonKembali = findViewById(R.id.buttonKembali);

        dbHelper = new DatabaseHelper(this);

        adapter = new HistoryAdapter(null);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        buttonKembali.setOnClickListener(v -> finish());

        // TUGAS LUIZA (Issue  #3)
        buttonHapusRiwayat.setOnClickListener(v -> tampilkanDialogHapus());
        muatRiwayat();
    }

    private void muatRiwayat() {
        executor.execute(() -> {
            Cursor cursor = dbHelper.ambilSemuaSesi();
            int jumlah = cursor.getCount();   // dipaksa terbaca di background thread

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    cursor.close();
                    return;
                }
                adapter.swapCursor(cursor);
                textKosong.setVisibility(jumlah == 0 ? View.VISIBLE : View.GONE);
                rvHistory.setVisibility(jumlah == 0 ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void tampilkanDialogHapus() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Riwayat")
                .setMessage("Apakah Anda yakin ingin menghapus seluruh riwayat permainan?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    executor.execute(() -> {
                        dbHelper.hapusSemuaRiwayat();
                        runOnUiThread(() -> {
                            muatRiwayat();
                            Toast.makeText(this, "Seluruh riwayat berhasil dihapus!", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        adapter.swapCursor(null);
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}