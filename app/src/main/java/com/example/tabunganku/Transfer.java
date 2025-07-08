package com.example.tabunganku;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class Transfer extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvDompetUtama;
    private EditText etJumlah;
    private ImageView ivBCA, ivGopay, ivDana;

    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private DatabaseReference dompetRef;
    private ValueEventListener dompetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        // Inisialisasi Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Sesi tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        dompetRef = dbRef.child("dompetUtama");

        // Inisialisasi UI
        btnBack = findViewById(R.id.btnBack);
        tvDompetUtama = findViewById(R.id.tvDompetUtama);
        etJumlah = findViewById(R.id.editTextText);
        ivBCA = findViewById(R.id.ivBCA);
        ivGopay = findViewById(R.id.ivGopay);
        ivDana = findViewById(R.id.ivDana);

        // Tombol kembali
        btnBack.setOnClickListener(v -> finish());

        // Klik metode transfer
        ivBCA.setOnClickListener(v -> prosesTransfer("BCA"));
        ivGopay.setOnClickListener(v -> prosesTransfer("Gopay"));
        ivDana.setOnClickListener(v -> prosesTransfer("DANA"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (dompetListener == null) {
            dompetListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Long saldo = snapshot.getValue(Long.class);
                    if (saldo == null) saldo = 0L;
                    NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
                    tvDompetUtama.setText("Dompet Utama\t\t" + format.format(saldo));
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(Transfer.this, "Gagal memuat saldo", Toast.LENGTH_SHORT).show();
                }
            };
            dompetRef.addValueEventListener(dompetListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dompetListener != null) {
            dompetRef.removeEventListener(dompetListener);
            dompetListener = null;
        }
    }

    private void prosesTransfer(String metode) {
        String jumlahStr = etJumlah.getText().toString().trim();

        if (TextUtils.isEmpty(jumlahStr)) {
            etJumlah.setError("Jumlah tidak boleh kosong");
            return;
        }

        long jumlahTransfer;
        try {
            jumlahTransfer = Long.parseLong(jumlahStr);
            if (jumlahTransfer <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etJumlah.setError("Jumlah tidak valid");
            return;
        }

        // Transaksi untuk mengurangi saldo dompet
        dompetRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Long saldoSaatIni = currentData.getValue(Long.class);
                if (saldoSaatIni == null) saldoSaatIni = 0L;

                if (jumlahTransfer > saldoSaatIni) {
                    return Transaction.abort();
                }

                currentData.setValue(saldoSaatIni - jumlahTransfer);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error == null && committed) {
                    catatRiwayatTransfer(metode, jumlahTransfer);
                } else if (!committed) {
                    Toast.makeText(Transfer.this, "Saldo tidak mencukupi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Transfer.this, "Transfer gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void catatRiwayatTransfer(String metode, long jumlah) {
        DatabaseReference riwayatRef = dbRef.child("riwayat").push();
        long timestamp = new Date().getTime();

        // Gunakan struktur dari Riwayat.java milikmu
        Riwayat riwayat = new Riwayat(
                "Transfer",        // jenis
                "Transfer ke " + metode, // keterangan
                jumlah,
                timestamp
        );

        riwayatRef.setValue(riwayat).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Berhasil transfer ke " + metode, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal mencatat riwayat", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
