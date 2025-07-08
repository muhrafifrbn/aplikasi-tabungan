package com.example.tabunganku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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
    private AppCompatButton btnAmbil, btnMenabung;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbReference;

    private String tabunganId;
    private NumberFormat format = NumberFormat.getInstance(new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tabungan);

        // Ambil ID tabungan dari intent
        tabunganId = getIntent().getStringExtra("TABUNGAN_ID");

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        // Validasi user dan tabungan
        if (currentUser == null || tabunganId == null || tabunganId.isEmpty()) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi komponen UI
        tvNamaTabungan = findViewById(R.id.tvDetailNamaTabungan);
        tvTarget = findViewById(R.id.tvDetailTarget);
        tvTerkumpul = findViewById(R.id.tvDetailTerkumpul);
        btnAmbil = findViewById(R.id.btnAmbil);
        btnMenabung = findViewById(R.id.btnMenabung);
        btnBack = findViewById(R.id.btnBack);

        // Tombol kembali
        btnBack.setOnClickListener(v -> finish());

        // Tombol ambil tabungan → ke UangKeluarActivity
        btnAmbil.setOnClickListener(v -> {
            Intent intent = new Intent(DetailTabunganActivity.this, UangKeluarActivity.class);
            intent.putExtra("TABUNGAN_ID", tabunganId);
            startActivity(intent);
        });

        // Tombol menabung → ke UangMasukActivity
        btnMenabung.setOnClickListener(v -> {
            Intent intent = new Intent(DetailTabunganActivity.this, UangMasukActivity.class);
            intent.putExtra("TABUNGAN_ID", tabunganId);
            startActivity(intent);
        });

        // Load data tabungan
        loadTabunganDetails();
    }

    private void loadTabunganDetails() {
        DatabaseReference tabunganRef = dbReference
                .child("users")
                .child(currentUser.getUid())
                .child("tabungan")
                .child(tabunganId);

        tabunganRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Tabungan tabungan = snapshot.getValue(Tabungan.class);
                    if (tabungan != null) {
                        tvNamaTabungan.setText(tabungan.getNama());
                        tvTarget.setText("Rp. " + format.format(tabungan.getTarget()));
                        tvTerkumpul.setText("Rp. " + format.format(tabungan.getTerkumpul()));
                    }
                } else {
                    Toast.makeText(DetailTabunganActivity.this, "Data tabungan tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DetailTabunganActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
