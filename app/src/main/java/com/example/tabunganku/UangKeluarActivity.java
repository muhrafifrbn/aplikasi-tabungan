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

import java.text.NumberFormat;
import java.util.Locale;

public class UangKeluarActivity extends AppCompatActivity {

    private EditText etJumlah;
    private Button btnTambah;
    private ImageButton btnBack;

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

        // Inisialisasi UI
        etJumlah = findViewById(R.id.editTextText);
        btnTambah = findViewById(R.id.btnTambah);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Ambil tabunganId dari Intent
        tabunganId = getIntent().getStringExtra("tabunganId");
        if (tabunganId == null) {
            Toast.makeText(this, "ID Tabungan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnTambah.setOnClickListener(v -> ambilTabungan());
    }

    private void ambilTabungan() {
        String jumlahStr = etJumlah.getText().toString().trim();
        if (jumlahStr.isEmpty()) {
            Toast.makeText(this, "Jumlah tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        long jumlahAmbil = Long.parseLong(jumlahStr);
        if (jumlahAmbil <= 0) {
            Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference tabunganRef = dbRef.child("tabungan").child(tabunganId).child("terkumpul");

        tabunganRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long jumlahTerkumpul = snapshot.getValue(Long.class);
                if (jumlahTerkumpul == null) jumlahTerkumpul = 0L;

                if (jumlahTerkumpul < jumlahAmbil) {
                    Toast.makeText(UangKeluarActivity.this, "Jumlah tabungan tidak cukup", Toast.LENGTH_SHORT).show();
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

                        // Simpan riwayat transaksi
                        DatabaseReference riwayatRef = dbRef.child("tabungan").child(tabunganId).child("riwayat").push();
                        String keteranganRiwayat = "Mengambil sejumlah " + formatNumber(jumlahAmbil);
                        Riwayat riwayat = new Riwayat("Ambil Tabungan", keteranganRiwayat, jumlahAmbil, System.currentTimeMillis());
                        riwayatRef.setValue(riwayat);

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

    private String formatNumber(long number) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(number);
    }
}