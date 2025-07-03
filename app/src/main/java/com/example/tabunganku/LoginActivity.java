package com.example.tabunganku; // Pastikan ini sesuai dengan nama paket aplikasi Anda

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Deklarasi komponen UI
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView txtSignUpHere; // Nama variabel dari kode Anda
    private ProgressBar progressBar;

    // Deklarasi instance Firebase Auth
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Hubungkan komponen UI dengan ID di XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        // Menggunakan ID dari kode Anda: txtSignInHere, namun variabelnya txtSignUpHere
        // Pastikan ID di XML adalah txtSignUpHere atau sesuaikan nama variabel ini
        txtSignUpHere = findViewById(R.id.txtSignUpHere);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        txtSignUpHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Cek apakah pengguna sudah login saat aplikasi dimulai.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // *** PERUBAHAN DI SINI ***
            // Periksa juga apakah emailnya sudah diverifikasi
            if(currentUser.isEmailVerified()){
                // Jika sudah login DAN terverifikasi, baru langsung masuk.
                Toast.makeText(this, "Selamat datang kembali!", Toast.LENGTH_SHORT).show();
                redirectToMainPage();
            }
            // Jika tidak terverifikasi, tidak melakukan apa-apa, tetap di halaman login.
        }
    }

    private void loginUser() {
        // Ambil input dari EditText
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email harus diisi.");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password harus diisi.");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Proses login menggunakan Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Jika kredensial benar, cek status verifikasi email
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null && user.isEmailVerified()) {
                                // Jika email SUDAH diverifikasi, lanjutkan ke halaman utama
                                Log.d(TAG, "signInWithEmail:success (Email Verified)");
                                Toast.makeText(LoginActivity.this, "Login Berhasil.", Toast.LENGTH_SHORT).show();
                                redirectToMainPage();
                            } else {
                                // Jika email BELUM diverifikasi, tampilkan pesan dan logout
                                Log.w(TAG, "signInWithEmail:failure (Email Not Verified)");
                                Toast.makeText(LoginActivity.this, "Login Gagal. Harap verifikasi email Anda terlebih dahulu.", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Pastikan pengguna tidak bisa masuk
                            }
                        } else {
                            // Jika login gagal karena kredensial salah
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Login Gagal. Periksa kembali email dan password Anda.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void redirectToMainPage() {
// Pindah ke DaftarTabunganActivity
        Intent intent = new Intent(LoginActivity.this, DaftarTabunganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Tutup LoginActivity
    }
}
