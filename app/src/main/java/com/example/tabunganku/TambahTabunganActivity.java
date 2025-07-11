package com.example.tabunganku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TambahTabunganActivity extends AppCompatActivity {

    private EditText etNamaTarget, etTargetTabungan;
    private Button btnSimpan;

    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_tabungan);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        // Perbaiki dbReference untuk menunjuk ke root database, kemudian Anda bisa menunjuk ke child "users"
        dbReference = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        // Hubungkan komponen UI dengan ID di XML
        etNamaTarget = findViewById(R.id.etNamaTarget);
        etTargetTabungan = findViewById(R.id.etTargetTabungan);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Jika tidak ada user yang login, tutup activity ini
        if (currentUser == null) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Atur listener untuk tombol simpan
        btnSimpan.setOnClickListener(v -> {
            simpanTabungan();
        });
    }

    private void simpanTabungan() {
        String namaTarget = etNamaTarget.getText().toString().trim();
        String targetTabunganStr = etTargetTabungan.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(namaTarget)) {
            etNamaTarget.setError("Nama target tidak boleh kosong");
            etNamaTarget.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(targetTabunganStr)) {
            etTargetTabungan.setError("Target tabungan tidak boleh kosong");
            etTargetTabungan.requestFocus();
            return;
        }

        long targetTabungan;
        try {
            targetTabungan = Long.parseLong(targetTabunganStr);
        } catch (NumberFormatException e) {
            etTargetTabungan.setError("Masukkan angka yang valid");
            etTargetTabungan.requestFocus();
            return;
        }

        // Dapatkan path ke node "tabungan" user yang sedang login
        DatabaseReference userTabunganRef = dbReference.child("users").child(currentUser.getUid()).child("tabungan");

        // Gunakan push() untuk membuat ID unik untuk setiap item tabungan baru
        String tabunganId = userTabunganRef.push().getKey();

        // Buat objek Tabungan baru
        // Saldo terkumpul awal adalah 0
        Tabungan tabunganBaru = new Tabungan(namaTarget, targetTabungan, 0);

        if (tabunganId != null) {
            // PENTING: Set ID tabungan ke objek Tabungan sebelum menyimpannya
            tabunganBaru.setId(tabunganId);

            // Simpan objek ke database di bawah ID yang baru dibuat
            userTabunganRef.child(tabunganId).setValue(tabunganBaru)
                    .addOnSuccessListener(aVoid -> {
                        // Jika berhasil
                        Toast.makeText(TambahTabunganActivity.this, "Tabungan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                        // Tutup activity dan kembali ke halaman daftar
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Jika gagal
                        Toast.makeText(TambahTabunganActivity.this, "Gagal menambahkan tabungan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}