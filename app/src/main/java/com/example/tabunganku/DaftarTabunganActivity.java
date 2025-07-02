package com.example.tabunganku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DaftarTabunganActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private FirebaseUser currentUser;

    // UI Components
    private TextView tvDompetUtama;
    private RecyclerView recyclerViewTabungan;
    private TabunganAdapter adapter;

    // Firebase Listeners & References
    private ValueEventListener tabunganListener, dompetListener;
    private DatabaseReference tabunganRef, dompetRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_tabungan);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        // Hubungkan UI
        tvDompetUtama = findViewById(R.id.tvDompetUtama);
        ImageButton btnLogout = findViewById(R.id.btnLogout); // Pastikan ID ini benar di XML Anda
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Jika tidak ada user yang login, langsung ke halaman Login
        if (currentUser == null) {
            goToLogin();
            return;
        }

        // Definisikan path database di sini
        String uid = currentUser.getUid();
        dompetRef = dbReference.child("users").child(uid).child("dompetUtama");
        tabunganRef = dbReference.child("users").child(uid).child("tabungan");

        // Panggil method untuk setup UI
        setupRecyclerView();

        // Atur listener untuk tombol-tombol
        btnLogout.setOnClickListener(v -> logoutUser());
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(DaftarTabunganActivity.this, TambahTabunganActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recyclerViewTabungan = findViewById(R.id.recyclerViewTabungan);
        recyclerViewTabungan.setLayoutManager(new LinearLayoutManager(this));
        // Inisialisasi adapter kosong. Data akan diisi oleh listener.
        adapter = new TabunganAdapter();
        recyclerViewTabungan.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Mulai mendengarkan data saat activity terlihat oleh pengguna

        // 1. Listener untuk Dompet Utama
        if (dompetListener == null) {
            dompetListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        long saldo = snapshot.getValue(Long.class);
                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
                        tvDompetUtama.setText("Dompet Utama\t\t" + format.format(saldo));
                    } else {
                        tvDompetUtama.setText("Dompet Utama\t\t0");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DaftarTabunganActivity.this, "Gagal memuat saldo.", Toast.LENGTH_SHORT).show();
                }
            };
            dompetRef.addValueEventListener(dompetListener);
        }

        // 2. Listener untuk Daftar Tabungan
        if (tabunganListener == null) {
            tabunganListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Tabungan> newList = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        // Ambil objek Tabungan
                        Tabungan tabungan = itemSnapshot.getValue(Tabungan.class);
                        if (tabungan != null) {
                            // Ambil ID unik dari snapshot
                            String key = itemSnapshot.getKey();
                            // Set ID tersebut ke dalam objek Tabungan
                            tabungan.setId(key);
                            // Tambahkan objek yang sudah lengkap ke list
                            newList.add(tabungan);
                        }
                    }
                    adapter.setData(newList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DaftarTabunganActivity.this, "Gagal memuat data tabungan.", Toast.LENGTH_SHORT).show();
                }
            };
            tabunganRef.addValueEventListener(tabunganListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hentikan listener saat activity tidak lagi terlihat untuk menghemat baterai dan data
        if (dompetListener != null) {
            dompetRef.removeEventListener(dompetListener);
            dompetListener = null; // Set null agar dibuat ulang di onStart()
        }
        if (tabunganListener != null) {
            tabunganRef.removeEventListener(tabunganListener);
            tabunganListener = null; // Set null agar dibuat ulang di onStart()
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(DaftarTabunganActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}