package com.example.tabunganku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailTabunganActivity extends AppCompatActivity {

    private TextView tvNamaTabungan, tvTarget, tvTerkumpul;
    private String tabunganId;
    private ImageButton btnBack;
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tabungan);

        // Ambil ID yang dikirim dari adapter
        tabunganId = getIntent().getStringExtra("TABUNGAN_ID");

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        // Hubungkan UI
        tvNamaTabungan = findViewById(R.id.tvDetailNamaTabungan);
        tvTarget = findViewById(R.id.tvDetailTarget);
        tvTerkumpul = findViewById(R.id.tvDetailTerkumpul);
        btnBack = findViewById(R.id.btnBack);

        // Validasi, jika tidak ada user atau ID, tutup activity
        if (currentUser == null || tabunganId == null) {
            Toast.makeText(this, "Data tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Panggil fungsi untuk memuat data dari Firebase
        loadTabunganDetails();

        btnBack.setOnClickListener(v -> {
            // Menutup activity saat ini dan kembali ke activity sebelumnya (DaftarTabunganActivity)
            finish();
        });
    }

    private void loadTabunganDetails() {
        // Buat referensi langsung ke item tabungan yang spesifik
        DatabaseReference tabunganRef = dbReference.child("users")
                .child(currentUser.getUid())
                .child("tabungan")
                .child(tabunganId);

        // Tambahkan listener untuk membaca data
        tabunganRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Konversi data dari snapshot ke objek Tabungan
                    Tabungan tabungan = snapshot.getValue(Tabungan.class);
                    if (tabungan != null) {
                        // Format angka
                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        // Set data ke TextViews
                        tvNamaTabungan.setText(tabungan.getNama());
                        tvTarget.setText("Rp. " + format.format(tabungan.getTarget()));
                        tvTerkumpul.setText("Rp. " + format.format(tabungan.getTerkumpul()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailTabunganActivity.this, "Gagal memuat detail.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}