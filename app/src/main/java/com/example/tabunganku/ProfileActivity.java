package com.example.tabunganku;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private EditText inputNama, inputNIK, inputTelepon, inputEmail, inputPassword;
    private Button btnUpdate, btnBack, btnLogOut;
    private TextView txtBack;
    private ProgressBar progressBar;
    private ImageView passwordToggle; // ImageView untuk ikon mata

    // Deklarasi instance Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        iniComponents();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        getDataCurrentUser();


//       Kembali Ke Halaman Activity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DaftarTabunganActivity.class));
                finish();
            }
        });

//        Update Data User
        btnUpdate.setOnClickListener((v) -> updateUser());

//        Tombol untuk LogOut
        btnLogOut.setOnClickListener((v) -> logoutUser());
    }

    public void  iniComponents(){
        // Hubungkan komponen UI dengan ID di XML
        inputNama = findViewById(R.id.inputNama);
        inputNIK = findViewById(R.id.inputNIK);
        inputTelepon = findViewById(R.id.inputTelepon);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
        btnLogOut = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar); // Hubungkan ProgressBar
    }

//    Fungsi untuk mengambil data user yang saat ini sedang login
    private void getDataCurrentUser(){
        // Mendapatkan referensi ke Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Mengambil data pengguna berdasarkan UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.child("users").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Jika berhasil, ambil data pengguna
                DataSnapshot dataSnapshot = task.getResult();
                String email = dataSnapshot.child("email").getValue(String.class);
                String namaLengkap = dataSnapshot.child("namaLengkap").getValue(String.class);
                String nik = dataSnapshot.child("nik").getValue(String.class);
                String nomorTelepon = dataSnapshot.child("nomorTelepon").getValue(String.class);

                inputNama.setText(namaLengkap);
                inputEmail.setText(email);
                inputNIK.setText(nik);
                inputTelepon.setText(nomorTelepon);

                // Menampilkan data pengguna
                Log.d("User Info", "Email: " + email);
                Log.d("User Info", "Nama Lengkap: " + namaLengkap);
                Log.d("User Info", "NIK: " + nik);
                Log.d("User Info", "Nomor Telepon: " + nomorTelepon);
            } else {
                Log.d("User Info", "Failed to retrieve user data.");
            }
        });
    }

    private void updateUser() {
        final String nama = inputNama.getText().toString().trim();
        final String nik = inputNIK.getText().toString().trim();
        final String telepon = inputTelepon.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(nama)) {
            inputNama.setError("Nama lengkap harus diisi.");
            inputNama.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(nik) || nik.length() != 16) {
            inputNIK.setError("NIK harus terdiri dari 16 digit.");
            inputNIK.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(telepon) || telepon.length() < 6 || telepon.length() > 16) {
            inputTelepon.setError("Nomor telepon harus 6-16 digit.");
            inputTelepon.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Format email tidak valid.");
            inputEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            inputPassword.setError("Password minimal harus 6 karakter.");
            inputPassword.requestFocus();
            return;
        }

        // Tampilkan ProgressBar sebelum memulai proses
        progressBar.setVisibility(View.VISIBLE);

        // Cek Keunikan NIK di Realtime Database
        databaseReference.orderByChild("nik").equalTo(nik)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Jika NIK sudah ada, sembunyikan ProgressBar dan tampilkan pesan
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "NIK sudah terdaftar.", Toast.LENGTH_LONG).show();
                        } else {
                            // NIK unik, lanjutkan pendaftaran (ProgressBar tetap terlihat)
//                            createFirebaseUser(email, password, nama, nik, telepon);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "NIK Belum terdaftar.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Jika gagal, sembunyikan ProgressBar dan tampilkan pesan error
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}