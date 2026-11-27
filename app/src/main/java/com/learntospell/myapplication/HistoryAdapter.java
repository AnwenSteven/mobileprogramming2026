package com.learntospell.myapplication;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Cursor cursor;

    public HistoryAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) {
            return;
        }

        String nama = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESI_NAMA_PLAYER));
        String level = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESI_LEVEL));
        int benar = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESI_BENAR));
        int salah = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESI_SALAH));
        int skor = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESI_SKOR));
        String waktu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESI_CATATAN_WAKTU));

        holder.textHasil.setText(nama + " - Level " + level);
        holder.textSkor.setText("Skor: " + skor + " (Benar: " + benar + ", Salah: " + salah + ")");
        holder.textWaktu.setText(waktu);
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textHasil, textSkor, textWaktu;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textHasil = itemView.findViewById(R.id.textHasil);
            textSkor = itemView.findViewById(R.id.textSkor);
            textWaktu = itemView.findViewById(R.id.textWaktu);
        }
    }
}