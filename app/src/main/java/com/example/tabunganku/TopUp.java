package com.example.tabunganku;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class TopUp extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private EditText etJumlahTopUp;
    private TextView tvDompetUtama;
    private ImageView ivBCA, ivGopay, ivDana;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private FirebaseUser currentUser;


    private ValueEventListener dompetListener;
    private DatabaseReference dompetRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        // Hubungkan UI
        btnBack = findViewById(R.id.btnBack);
        etJumlahTopUp = findViewById(R.id.etJumlahTopUp);
        tvDompetUtama = findViewById(R.id.tvDompetUtama);
        ivBCA = findViewById(R.id.ivBCA);
        ivGopay = findViewById(R.id.ivGopay);
        ivDana = findViewById(R.id.ivDana);

        // Validasi user session
        if (currentUser == null) {
            Toast.makeText(this, "Sesi tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Definisikan path database di sini
        dompetRef = dbReference.child("users").child(currentUser.getUid()).child("dompetUtama");

        setClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Mulai mendengarkan data saldo saat activity terlihat
        if (dompetListener == null) {
            dompetListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        long saldo = snapshot.getValue(Long.class);
                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
                        tvDompetUtama.setText("Dompet Utama\t\t" + format.format(saldo));
                    } else {
                        tvDompetUtama.setText("Dompet Utama\t\t0");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TopUp.this, "Gagal memuat saldo.", Toast.LENGTH_SHORT).show();
                }
            };
            dompetRef.addValueEventListener(dompetListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hentikan listener saat activity tidak terlihat untuk menghemat resource
        if (dompetListener != null) {
            dompetRef.removeEventListener(dompetListener);
            dompetListener = null;
        }
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        ivBCA.setOnClickListener(v -> prosesTopUp("BCA"));
        ivGopay.setOnClickListener(v -> prosesTopUp("Gopay"));
        ivDana.setOnClickListener(v -> prosesTopUp("DANA"));
    }

    private void prosesTopUp(String metodePembayaran) {
        String jumlahStr = etJumlahTopUp.getText().toString().trim();

        if (TextUtils.isEmpty(jumlahStr)) {
            etJumlahTopUp.setError("Jumlah tidak boleh kosong");
            return;
        }

        long jumlahTopUp;
        try {
            jumlahTopUp = Long.parseLong(jumlahStr);
        } catch (NumberFormatException e) {
            etJumlahTopUp.setError("Jumlah tidak valid");
            return;
        }

        if (jumlahTopUp <= 0) {
            etJumlahTopUp.setError("Jumlah harus lebih dari 0");
            return;
        }

        // Menggunakan Transaksi untuk update saldo dengan aman
        dompetRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long saldoSaatIni = currentData.getValue(Long.class);
                if (saldoSaatIni == null) {
                    saldoSaatIni = 0L;
                }
                long saldoBaru = saldoSaatIni + jumlahTopUp;
                currentData.setValue(saldoBaru);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error == null && committed) {
                    catatRiwayat(metodePembayaran, jumlahTopUp);
                } else {
                    Toast.makeText(TopUp.this, "Top up gagal, coba lagi.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void catatRiwayat(String metodePembayaran, long jumlah) {
        DatabaseReference riwayatRef = dbReference.child("users").child(currentUser.getUid()).child("riwayat").push();
        long timestamp = new Date().getTime();

        Riwayat riwayatTopUp = new Riwayat("Top Up", metodePembayaran, jumlah, timestamp);

        riwayatRef.setValue(riwayatTopUp).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Top up berhasil!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal mencatat riwayat.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}