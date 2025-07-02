package com.example.tabunganku; // Pastikan ini sesuai dengan paket aplikasi Anda

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class DaftarTabunganActivity extends AppCompatActivity {

    private ImageButton btnLogout;
    private FloatingActionButton fabAdd;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan nama file layout XML Anda adalah activity_daftar_tabungan
        setContentView(R.layout.activity_daftar_tabungan);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Hubungkan komponen UI dengan ID di XML
        btnLogout = findViewById(R.id.btnLogout);
        fabAdd = findViewById(R.id.fabAdd);

        // Atur listener untuk tombol logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Atur listener untuk tombol tambah (FAB)
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Di sini Anda bisa menambahkan logika untuk pindah ke halaman tambah tabungan
                // Contoh:
                // startActivity(new Intent(DaftarTabunganActivity.this, TambahTabunganActivity.class));
                Toast.makeText(DaftarTabunganActivity.this, "Tombol Tambah diklik!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        // Lakukan sign out dari Firebase
        mAuth.signOut();

        // Tampilkan pesan konfirmasi
        Toast.makeText(DaftarTabunganActivity.this, "Anda telah logout.", Toast.LENGTH_SHORT).show();

        // Arahkan pengguna kembali ke LoginActivity
        Intent intent = new Intent(DaftarTabunganActivity.this, LoginActivity.class);
        // Hapus semua activity sebelumnya dari tumpukan (stack)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Tutup activity ini
    }
}
