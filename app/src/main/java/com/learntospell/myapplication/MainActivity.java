package com.learntospell.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, String> kamusEasy, kamusMedium, kamusDifficult, kamus;
    private ArrayList<String> kataEasy, kataMedium, kataDifficult, kata, listSalah;
    int JUMLAH_OPSI = 5, JUMLAH_SOAL = 10;       // JUMLAH_SOAL bisa diganti dari Pengaturan (via Intent)
    private RecyclerView rvOpsi;
    private OpsiAdapter adapter;
    private TextView textHasil;
    private String soal;
    private ArrayList<String> opsiJawaban;
    private static final String TAG_LC = "Lifecycle";
    private TextView textSkor;
    private int skorBenar = 0;
    private int skorSalah = 0;
    private int soalKe = 0;

    private static final String TAG_TIMER = "Timer";

    private TextView textWaktu;
    private RadioGroup radioGroupLevel;
    private CountDownTimer timer;
    private long durasiSoalMs = 15000;           // bisa diganti dari Pengaturan (via Intent)
    private long sisaMs = durasiSoalMs;
    private boolean sudahDijawab = false;        // true setelah soal dijawab / waktu habis

    private static final String KEY_SKOR_BENAR = "skor_benar";
    private static final String KEY_SKOR_SALAH = "skor_salah";
    private static final String KEY_SOAL = "soal";
    private static final String KEY_OPSI = "opsi";
    private static final String KEY_SISA_MS = "sisa_ms";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_SOAL_KE = "soal_ke";
    private static final String KEY_DIJAWAB = "dijawab";
    private static final String KEY_WAKTU_MULAI = "waktu_mulai";
    private static final String KEY_DET_KATA = "det_kata";
    private static final String KEY_DET_DIPILIH = "det_dipilih";
    private static final String KEY_DET_STATUS = "det_status";

    private DatabaseHelper dbHelper;
    private String namaPlayer = "Player";
    private ArrayList<DatabaseHelper.DetailJawaban> listDetailJawaban = new ArrayList<>();
    private long waktuMulaiSesiMs = 0;

    // Salinan detail dalam bentuk String, hanya supaya bisa disimpan di Bundle saat layar diputar
    private final ArrayList<String> catKata = new ArrayList<>();
    private final ArrayList<String> catDipilih = new ArrayList<>();
    private final ArrayList<String> catStatus = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG_LC, "onCreate, savedInstanceState " + (savedInstanceState == null ? "null" : "ada"));

        dbHelper = new DatabaseHelper(this);

        // Nama, jumlah soal, dan detik per soal dikirim dari MenuActivity
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("NAMA_PLAYER")) {
                namaPlayer = intent.getStringExtra("NAMA_PLAYER");
            }
            JUMLAH_SOAL = intent.getIntExtra("JUMLAH_SOAL", 10);
            int detik = intent.getIntExtra("DETIK_PER_SOAL", 15);
            durasiSoalMs = detik * 1000L;
        }
        sisaMs = durasiSoalMs;

        textHasil = findViewById(R.id.hasil);
        rvOpsi = findViewById(R.id.opsi);
        rvOpsi.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OpsiAdapter(new OpsiAdapter.OnOpsiClickListener() {
            @Override
            public void onOpsiClick(int position) {
                periksaJawaban(position);
                Log.d("VocabQuiz", "Opsi diklik: posisi " + position);
            }
        });
        rvOpsi.setAdapter(adapter);

        Button tombolBerikutnya = findViewById(R.id.tombolBerikutnya);
        tombolBerikutnya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sudahDijawab) {
                    Toast.makeText(MainActivity.this, "Jawab soal ini dulu", Toast.LENGTH_SHORT).show();
                    return;
                }
                letsPlay();
            }
        });

        kamus = new HashMap<>();
        kata = new ArrayList<>();
        listSalah = new ArrayList<>();

        kamusEasy = new HashMap<>();
        kataEasy = new ArrayList<>();
        kamusMedium = new HashMap<>();
        kataMedium = new ArrayList<>();
        kamusDifficult = new HashMap<>();
        kataDifficult = new ArrayList<>();

        readFromFile(R.raw.vocab_easy, kamusEasy, kataEasy);
        readFromFile(R.raw.vocab_medium, kamusMedium, kataMedium);
        readFromFile(R.raw.vocab_difficult, kamusDifficult, kataDifficult);
        kamus = kamusEasy;
        kata = kataEasy;
        textSkor = findViewById(R.id.skor);
        textWaktu = findViewById(R.id.waktu);
        radioGroupLevel = findViewById(R.id.radioGroupLevel);

        // ---- Pulihkan state (rotasi layar) SEBELUM listener level dipasang ----
        boolean lanjutkanSoal = false;
        if (savedInstanceState != null) {
            skorBenar = savedInstanceState.getInt(KEY_SKOR_BENAR);
            skorSalah = savedInstanceState.getInt(KEY_SKOR_SALAH);
            soalKe = savedInstanceState.getInt(KEY_SOAL_KE);
            waktuMulaiSesiMs = savedInstanceState.getLong(KEY_WAKTU_MULAI, System.currentTimeMillis());

            ArrayList<String> k = savedInstanceState.getStringArrayList(KEY_DET_KATA);
            ArrayList<String> d = savedInstanceState.getStringArrayList(KEY_DET_DIPILIH);
            ArrayList<String> s = savedInstanceState.getStringArrayList(KEY_DET_STATUS);
            if (k != null && d != null && s != null) {
                for (int i = 0; i < k.size(); i++) {
                    catatDetail(k.get(i), d.get(i), s.get(i));
                }
            }

            int idLevel = savedInstanceState.getInt(KEY_LEVEL);
            if (idLevel != 0) radioGroupLevel.check(idLevel);
            pilihLevel(radioGroupLevel.getCheckedRadioButtonId());

            // listSalah dibangun ulang dari detail yang tersimpan
            for (int i = 0; i < catKata.size(); i++) {
                if ("Salah".equals(catStatus.get(i))) {
                    listSalah.add(catKata.get(i) + ": " + kamus.get(catKata.get(i)));
                }
            }

            String soalTersimpan = savedInstanceState.getString(KEY_SOAL);
            ArrayList<String> opsiTersimpan = savedInstanceState.getStringArrayList(KEY_OPSI);
            boolean dijawab = savedInstanceState.getBoolean(KEY_DIJAWAB);
            if (soalTersimpan != null && opsiTersimpan != null && !dijawab) {
                soal = soalTersimpan;
                opsiJawaban = opsiTersimpan;
                sisaMs = savedInstanceState.getLong(KEY_SISA_MS, durasiSoalMs);
                lanjutkanSoal = true;
            }
        }

        // Ganti level di tengah sesi = mulai sesi baru (supaya skor & detail konsisten)
        radioGroupLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                pilihLevel(checkedId);
                mulaiSesiBaru();
            }
        });

        if (savedInstanceState == null) {
            mulaiSesiBaru();             // buka layar pertama kali
        } else if (lanjutkanSoal) {
            tampilkanSoal();             // timer dilanjutkan di onResume()
            updateSkor();
        } else {
            updateSkor();
            letsPlay();                  // soal terakhir sudah dijawab -> lanjut soal berikutnya
        }
    }

    private String kodelevel() {
        int id = radioGroupLevel.getCheckedRadioButtonId();
        if (id == R.id.radioEasy) return "Easy";
        if (id == R.id.radioMedium) return "Medium";
        return "Difficult";
    }

    // Satu pintu untuk mencatat detail jawaban (ke list untuk SQLite + salinan untuk Bundle)
    private void catatDetail(String kataSoal, String jawabanDipilih, String status) {
        listDetailJawaban.add(new DatabaseHelper.DetailJawaban(kataSoal, jawabanDipilih, status));
        catKata.add(kataSoal);
        catDipilih.add(jawabanDipilih);
        catStatus.add(status);
    }

    private void mulaiSesiBaru() {
        soalKe = 0;
        skorBenar = 0;
        skorSalah = 0;
        listSalah.clear();
        listDetailJawaban.clear();
        catKata.clear();
        catDipilih.clear();
        catStatus.clear();
        waktuMulaiSesiMs = System.currentTimeMillis();
        letsPlay();
    }

    private void selesaiSesi() {
        hentikanTimer();
        sudahDijawab = true;

        long totalWaktuDetik = (System.currentTimeMillis() - waktuMulaiSesiMs) / 1000;
        String catatanWaktu = totalWaktuDetik + " detik";
        int skorAkhir = (JUMLAH_SOAL > 0) ? (skorBenar * 100 / JUMLAH_SOAL) : 0;

        dbHelper.simpanSesi(namaPlayer, kodelevel(), skorBenar, skorSalah,
                skorAkhir, catatanWaktu, listDetailJawaban);

        Intent intent = new Intent(this, RingkasanHasil.class);
        intent.putExtra("NAMA_PLAYER", namaPlayer);
        intent.putExtra("JUMLAH_BENAR", skorBenar);
        intent.putExtra("JUMLAH_SALAH", skorSalah);
        intent.putExtra("JUMLAH_SOAL", JUMLAH_SOAL);
        intent.putExtra("LEVEL", kodelevel());
        intent.putStringArrayListExtra("DAFTAR_SALAH", new ArrayList<>(listSalah));  // salinan, karena akan di-clear

        startActivity(intent);

        mulaiSesiBaru();
    }

    private void pilihLevel(int checkedId) {
        if (checkedId == R.id.radioEasy) {
            kamus = kamusEasy;
            kata = kataEasy;
        } else if (checkedId == R.id.radioMedium) {
            kamus = kamusMedium;
            kata = kataMedium;
        } else if (checkedId == R.id.radioDifficult) {
            kamus = kamusDifficult;
            kata = kataDifficult;
        }
    }

    private void letsPlay() {
        if (soalKe >= JUMLAH_SOAL) {
            selesaiSesi();
            return;
        }

        sudahDijawab = false;
        soalKe++;
        opsiJawaban = new ArrayList<>();
        soal = null;

        ArrayList<String> acak = new ArrayList<>(kata);
        Collections.shuffle(acak);
        soal = acak.get(0);

        for (int i = 0; i < JUMLAH_OPSI; i++) {
            opsiJawaban.add(kamus.get(acak.get(i)));
        }
        Collections.shuffle(opsiJawaban);

        tampilkanSoal();
        updateSkor();

        sisaMs = durasiSoalMs;
        mulaiTimer(sisaMs);
    }

    // Menampilkan soal yang sedang aktif (dipakai letsPlay dan pemulihan rotasi)
    private void tampilkanSoal() {
        TextView textSoal = findViewById(R.id.vocab);
        textSoal.setText(soal);
        textHasil.setText("");
        adapter.setOpsi(opsiJawaban, opsiJawaban.indexOf(kamus.get(soal)));
    }

    private void periksaJawaban(int position) {
        hentikanTimer();
        sudahDijawab = true;
        adapter.tandaiJawaban(position);
        String benar = kamus.get(soal);
        String diklik = opsiJawaban.get(position);

        if (diklik.equals(benar)) {
            skorBenar++;
            textHasil.setText("Benar!");
            catatDetail(soal, diklik, "Benar");
        } else {
            skorSalah++;
            textHasil.setText("Salah. Jawaban benar: " + benar);
            listSalah.add(soal + ": " + benar);
            catatDetail(soal, diklik, "Salah");
        }
        updateSkor();
    }

    private void waktuHabis() {
        sudahDijawab = true;
        skorSalah++;
        updateSkor();
        adapter.tandaiWaktuHabis();
        String benar = kamus.get(soal);
        textHasil.setText("Waktu habis. Jawaban benar: " + benar);
        listSalah.add(soal + ": " + benar);
        catatDetail(soal, "Waktu Habis", "Salah");
    }

    private void readFromFile(int resourceId, HashMap<String, String> kamus, ArrayList<String> kata) {
        Scanner scan = new Scanner(getResources().openRawResource(resourceId));
        while (scan.hasNextLine()) {
            String[] t = scan.nextLine().split("\t");
            if (t.length >= 2) {
                kamus.put(t[0].trim(), t[1].trim());
                kata.add(t[0].trim());
            }
        }
        scan.close();
    }

    private void updateSkor() {
        textSkor.setText("Soal " + Math.max(soalKe, 1) + "/" + JUMLAH_SOAL
                + "  Benar: " + skorBenar + "  Salah: " + skorSalah);
    }

    private void mulaiTimer(long durasi) {
        hentikanTimer();
        timer = new CountDownTimer(durasi, 1000) {
            @Override
            public void onTick(long ms) {
                sisaMs = ms;
                textWaktu.setText((ms + 999) / 1000 + " detik");
                Log.d(TAG_TIMER, "tick " + (ms + 999) / 1000
                        + " dari activity " + Integer.toHexString(MainActivity.this.hashCode()));
            }

            @Override
            public void onFinish() {
                sisaMs = 0;
                textWaktu.setText("0 detik");
                waktuHabis();
            }
        }.start();
    }

    private void hentikanTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG_LC, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG_LC, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG_LC, "onResume");
        if (!sudahDijawab && sisaMs > 0) {
            mulaiTimer(sisaMs);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG_LC, "onPause");
        hentikanTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG_LC, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        Log.d(TAG_LC, "onDestroy, isFinishing=" + isFinishing()
                + ", isChangingConfigurations=" + isChangingConfigurations());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SKOR_BENAR, skorBenar);
        outState.putInt(KEY_SKOR_SALAH, skorSalah);
        outState.putInt(KEY_SOAL_KE, soalKe);
        outState.putBoolean(KEY_DIJAWAB, sudahDijawab);
        outState.putLong(KEY_WAKTU_MULAI, waktuMulaiSesiMs);
        outState.putStringArrayList(KEY_DET_KATA, catKata);
        outState.putStringArrayList(KEY_DET_DIPILIH, catDipilih);
        outState.putStringArrayList(KEY_DET_STATUS, catStatus);

        outState.putString(KEY_SOAL, soal);
        outState.putStringArrayList(KEY_OPSI, opsiJawaban);
        outState.putLong(KEY_SISA_MS, sisaMs);
        outState.putInt(KEY_LEVEL, radioGroupLevel.getCheckedRadioButtonId());
        Log.d(TAG_LC, "onSaveInstanceState, skor=" + skorBenar + "/" + skorSalah
                + radioGroupLevel.getCheckedRadioButtonId());
    }
}