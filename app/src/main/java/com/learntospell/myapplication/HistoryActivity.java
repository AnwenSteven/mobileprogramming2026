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

public class HistoryActivity extends AppCompatActivity {

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

        // TUGAS LUIZA (Issue #3)
        buttonHapusRiwayat.setOnClickListener(v -> tampilkanDialogHapus());
        muatRiwayat();
    }

    private void muatRiwayat() {
        Cursor cursor = dbHelper.ambilSemuaSesi();
        adapter.swapCursor(cursor);

        if (cursor.getCount() == 0) {
            textKosong.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            textKosong.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}