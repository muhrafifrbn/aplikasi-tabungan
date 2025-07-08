package com.example.tabunganku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.NumberFormat;
import java.util.Locale;

public class DaftarTabunganActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private FirebaseUser currentUser;
    private TextView tvDompetUtama;
    private ValueEventListener dompetListener;
    private DatabaseReference dompetRef;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private Button btnTransfer, btnTopUp, btnRiwayat; // Tambahkan btnRiwayat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_tabungan);

        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        tvDompetUtama = findViewById(R.id.tvDompetUtama);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnTopUp = findViewById(R.id.btnTopUp);

        if (currentUser == null) {
            goToLogin();
            return;
        }

        String uid = currentUser.getUid();
        dompetRef = dbReference.child("users").child(uid).child("dompetUtama");

        // Setup ViewPager dan TabLayout
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Tabungan");
            } else {
                tab.setText("Riwayat");
            }
        }).attach();

        // Aksi tombol-tombol
        btnLogout.setOnClickListener(v -> logoutUser());

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(DaftarTabunganActivity.this, TambahTabunganActivity.class));
        });

        btnTransfer.setOnClickListener(v -> {
            startActivity(new Intent(DaftarTabunganActivity.this, Transfer.class));
        });

        btnTopUp.setOnClickListener(v -> {
            startActivity(new Intent(DaftarTabunganActivity.this, TopUp.class));
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
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
                    Toast.makeText(DaftarTabunganActivity.this, "Gagal memuat saldo.", Toast.LENGTH_SHORT).show();
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

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(DaftarTabunganActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
