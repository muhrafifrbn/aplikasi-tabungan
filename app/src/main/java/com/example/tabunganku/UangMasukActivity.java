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

public class UangMasukActivity extends AppCompatActivity {

    private EditText etJumlah;
    private Button btnTambah;
    private ImageButton btnBack;

    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private String tabunganId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uang_masuk);

        // Inisialisasi UI
        etJumlah = findViewById(R.id.editTextText);
        btnTambah = findViewById(R.id.btnTambah);
        btnBack = findViewById(R.id.btnBack); // Tambahkan tombol kembali

        btnBack.setOnClickListener(v -> finish()); // Aksi tombol kembali

        // Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Ambil tabunganId dari intent
        tabunganId = getIntent().getStringExtra("TABUNGAN_ID");
        if (tabunganId == null || tabunganId.isEmpty()) {
            Toast.makeText(this, "Tabungan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnTambah.setOnClickListener(v -> transferKeTabungan());
    }

    private void transferKeTabungan() {
        String input = etJumlah.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Masukkan jumlah terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        long jumlahMasuk;
        try {
            jumlahMasuk = Long.parseLong(input);
            if (jumlahMasuk <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference dompetRef = dbRef.child("dompetUtama");

        dompetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long saldoDompet = snapshot.getValue(Long.class);
                if (saldoDompet == null || saldoDompet < jumlahMasuk) {
                    Toast.makeText(UangMasukActivity.this, "Saldo dompet tidak mencukupi", Toast.LENGTH_SHORT).show();
                    return;
                }

                long sisaDompet = saldoDompet - jumlahMasuk;

                DatabaseReference tabunganRef = dbRef.child("tabungan").child(tabunganId).child("terkumpul");

                tabunganRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Long terkumpul = snapshot.getValue(Long.class);
                        if (terkumpul == null) terkumpul = 0L;

                        long totalBaru = terkumpul + jumlahMasuk;

                        // Simpan perubahan
                        dompetRef.setValue(sisaDompet);
                        tabunganRef.setValue(totalBaru);

                        Toast.makeText(UangMasukActivity.this, "Berhasil menabung", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(UangMasukActivity.this, "Gagal mengambil data tabungan", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UangMasukActivity.this, "Gagal mengambil data dompet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
