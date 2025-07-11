package com.example.tabunganku;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

public class UangKeluarActivity extends AppCompatActivity {

    private EditText etJumlah;
    private Button btnTambah;
    private ImageButton btnBack; // Tambahan tombol kembali

    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private String tabunganId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uang_keluar);

        // Inisialisasi Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Ambil tabungan ID dari intent
        tabunganId = getIntent().getStringExtra("TABUNGAN_ID");
        if (tabunganId == null || tabunganId.isEmpty()) {
            Toast.makeText(this, "Tabungan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi UI
        etJumlah = findViewById(R.id.editTextText);
        btnTambah = findViewById(R.id.btnTambah);
        btnBack = findViewById(R.id.btnBack); // Inisialisasi tombol kembali

        btnBack.setOnClickListener(v -> finish()); // Aksi kembali

        btnTambah.setOnClickListener(v -> ambilTabungan());
    }

    private void ambilTabungan() {
        String input = etJumlah.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Masukkan jumlah terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        long jumlahAmbil;
        try {
            jumlahAmbil = Long.parseLong(input);
            if (jumlahAmbil <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference tabunganTerkumpulRef = dbRef.child("tabungan").child(tabunganId).child("terkumpul");

        tabunganTerkumpulRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long jumlahTerkumpul = snapshot.getValue(Long.class);
                if (jumlahTerkumpul == null || jumlahTerkumpul < jumlahAmbil) {
                    Toast.makeText(UangKeluarActivity.this, "Saldo tabungan tidak mencukupi", Toast.LENGTH_SHORT).show();
                    return;
                }

                long sisaTabungan = jumlahTerkumpul - jumlahAmbil;

                // Ambil saldo dompet utama
                dbRef.child("dompetUtama").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Long saldoDompet = snapshot.getValue(Long.class);
                        if (saldoDompet == null) saldoDompet = 0L;

                        long saldoBaru = saldoDompet + jumlahAmbil;

                        // Simpan perubahan
                        dbRef.child("tabungan").child(tabunganId).child("terkumpul").setValue(sisaTabungan);
                        dbRef.child("dompetUtama").setValue(saldoBaru);

                        Toast.makeText(UangKeluarActivity.this, "Berhasil mengambil tabungan", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(UangKeluarActivity.this, "Gagal mengambil saldo dompet", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UangKeluarActivity.this, "Gagal mengambil data tabungan", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
