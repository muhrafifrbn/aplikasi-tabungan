package com.example.tabunganku;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailTabunganActivity extends AppCompatActivity {

    private TextView tvNamaTabungan, tvTarget, tvTerkumpul;
    private RecyclerView rvRiwayat;
    private AppCompatButton btnAmbil, btnMenabung;
    private ImageButton btnBack, btnEditNamaTabungan, btnDeleteTabungan; // Tambahkan btnDeleteTabungan

    private DetailTabunganAdapter riwayatAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbReference;
    private String tabunganId;
    private long currentTerkumpulAmount = 0; // Variabel untuk menyimpan jumlah terkumpul saat ini

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
        btnEditNamaTabungan = findViewById(R.id.btnEditNamaTabungan);
        btnDeleteTabungan = findViewById(R.id.btnDeleteTabungan); // Inisialisasi tombol hapus

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

        // Listener untuk tombol edit nama tabungan
        btnEditNamaTabungan.setOnClickListener(v -> showEditNamaTabunganDialog());

        // Listener untuk tombol hapus tabungan
        btnDeleteTabungan.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    // Simpan jumlah terkumpul saat ini
                    currentTerkumpulAmount = tabungan.getTerkumpul();
                } else {
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
                } else {
                    rvRiwayat.setVisibility(View.VISIBLE);
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

    private void showEditNamaTabunganDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Nama Tabungan");

        final EditText input = new EditText(this);
        input.setText(tvNamaTabungan.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(DetailTabunganActivity.this, "Nama tabungan tidak boleh kosong", Toast.LENGTH_SHORT).show();
            } else {
                updateNamaTabungan(newName);
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateNamaTabungan(String newName) {
        if (currentUser == null || tabunganId == null) {
            Toast.makeText(this, "Kesalahan: User atau ID tabungan tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference tabunganRef = dbReference.child("tabungan").child(tabunganId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("nama", newName);

        tabunganRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailTabunganActivity.this, "Nama tabungan berhasil diubah!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailTabunganActivity.this, "Gagal mengubah nama tabungan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DetailTabunganActivity", "Error updating tabungan name", e);
                });
    }

    // --- Metode untuk Hapus Tabungan dengan Kondisi Saldo ---

    private void showDeleteConfirmationDialog() {
        // Cek kondisi saldo terkumpul
        if (currentTerkumpulAmount > 0) {
            // Jika ada saldo, tampilkan notifikasi bahwa tidak bisa dihapus
            new AlertDialog.Builder(this)
                    .setTitle("Tidak Dapat Menghapus Tabungan")
                    .setMessage("Tabungan ini memiliki saldo yang terkumpul. Harap kosongkan saldo (Rp 0) sebelum menghapusnya.")
                    .setPositiveButton("Oke", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            // Jika saldo 0, lanjutkan dengan dialog konfirmasi hapus
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Tabungan?")
                    .setMessage("Apakah Anda yakin ingin menghapus tabungan ini? Riwayat tabungan juga akan terhapus secara permanen.")
                    .setPositiveButton("Hapus", (dialog, which) -> deleteTabungan())
                    .setNegativeButton("Batal", (dialog, which) -> dialog.cancel())
                    .show();
        }
    }

    private void deleteTabungan() {
        if (currentUser == null || tabunganId == null) {
            Toast.makeText(this, "Kesalahan: User atau ID tabungan tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference tabunganRef = dbReference.child("tabungan").child(tabunganId);
        tabunganRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailTabunganActivity.this, "Tabungan berhasil dihapus.", Toast.LENGTH_SHORT).show();
                    finish(); // Kembali ke halaman sebelumnya setelah tabungan dihapus
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailTabunganActivity.this, "Gagal menghapus tabungan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DetailTabunganActivity", "Error deleting tabungan", e);
                });
    }

    private String formatNumber(long number) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(number);
    }
}