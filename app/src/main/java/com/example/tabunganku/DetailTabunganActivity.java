package com.example.tabunganku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DetailTabunganActivity extends AppCompatActivity {

    private TextView tvNamaTabungan, tvTarget, tvTerkumpul;
    private RecyclerView rvRiwayat;
    private AppCompatButton btnAmbil, btnMenabung;
    private ImageButton btnBack;

    private DetailTabunganAdapter riwayatAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbReference;
    private String tabunganId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tabungan);

        // Inisialisasi UI
        tvNamaTabungan = findViewById(R.id.tvDetailNamaTabungan);
        tvTarget = findViewById(R.id.tvDetailTarget);
        tvTerkumpul = findViewById(R.id.tvDetailTerkumpul);
        rvRiwayat = findViewById(R.id.rvRiwayat);
        btnAmbil = findViewById(R.id.btnAmbil);
        btnMenabung = findViewById(R.id.btnMenabung);
        btnBack = findViewById(R.id.btnBack);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dbReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Ambil tabunganId dari Intent
        tabunganId = getIntent().getStringExtra("tabunganId");
//        Log.d("id", tabunganId);
        if (tabunganId == null) {
            Toast.makeText(this, "ID Tabungan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView
        riwayatAdapter = new DetailTabunganAdapter(this);
        rvRiwayat.setLayoutManager(new LinearLayoutManager(this));
        rvRiwayat.setAdapter(riwayatAdapter);

        // Muat data tabungan dan riwayat
        loadTabunganDetail();
        loadRiwayatTabungan();

        // Listener tombol
        btnBack.setOnClickListener(v -> finish());
        btnMenabung.setOnClickListener(v -> {
            Intent intent = new Intent(DetailTabunganActivity.this, UangMasukActivity.class);
            intent.putExtra("tabunganId", tabunganId);
            startActivity(intent);
        });
        btnAmbil.setOnClickListener(v -> {
            Intent intent = new Intent(DetailTabunganActivity.this, UangKeluarActivity.class);
            intent.putExtra("tabunganId", tabunganId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Muat ulang data saat kembali ke activity ini
        loadTabunganDetail();
        loadRiwayatTabungan();
    }

    private void loadTabunganDetail() {
        DatabaseReference tabunganRef = dbReference.child("tabungan").child(tabunganId);
        tabunganRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Tabungan tabungan = snapshot.getValue(Tabungan.class);
                if (tabungan != null) {
                    tvNamaTabungan.setText(tabungan.getNama());
                    tvTarget.setText(formatNumber(tabungan.getTarget()));
                    tvTerkumpul.setText(formatNumber(tabungan.getTerkumpul()));
                } else {
                    // Jika tabungan tidak ditemukan (mungkin sudah dihapus)
                    Toast.makeText(DetailTabunganActivity.this, "Tabungan tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailTabunganActivity.this, "Gagal memuat detail tabungan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRiwayatTabungan() {
        DatabaseReference riwayatListRef = dbReference.child("tabungan")
                .child(tabunganId)
                .child("riwayat");

        riwayatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Riwayat> listRiwayat = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot riwayatSnapshot : snapshot.getChildren()) {
                        Riwayat riwayat = riwayatSnapshot.getValue(Riwayat.class);
                        if (riwayat != null) {
                            listRiwayat.add(riwayat);
                        }
                    }
                }

                if (listRiwayat.isEmpty()) {
                    rvRiwayat.setVisibility(View.GONE);
                    // Jika Anda punya TextView "Riwayat kosong", tampilkan di sini
                    // Contoh: findViewById(R.id.tvEmptyRiwayatMessage).setVisibility(View.VISIBLE);
                } else {
                    rvRiwayat.setVisibility(View.VISIBLE);
                    // Contoh: findViewById(R.id.tvEmptyRiwayatMessage).setVisibility(View.GONE);

                    // Mengurutkan berdasarkan timestamp, dari yang paling BARU ke yang paling LAMA
                    Collections.sort(listRiwayat, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
                    riwayatAdapter.setData(listRiwayat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailTabunganActivity.this, "Gagal memuat riwayat: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatNumber(long number) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(number);
    }
}