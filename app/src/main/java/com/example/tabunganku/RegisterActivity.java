package com.example.tabunganku; // Ganti dengan nama paket aplikasi Anda

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Impor ImageView untuk ikon mata
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
// Impor untuk Realtime Database
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Deklarasi komponen UI
    private EditText inputNama, inputNIK, inputTelepon, inputEmail, inputPassword;
    private Button btnRegister;
    private TextView txtLoginHere;
    private ProgressBar progressBar;
    private ImageView passwordToggle; // ImageView untuk ikon mata

    // Deklarasi instance Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private static final String TAG = "RegisterActivity";
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Hubungkan komponen UI dengan ID di XML
        inputNama = findViewById(R.id.inputNama);
        inputNIK = findViewById(R.id.inputNIK);
        inputTelepon = findViewById(R.id.inputTelepon);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLoginHere = findViewById(R.id.txtLoginHere);
        progressBar = findViewById(R.id.progressBar); // Hubungkan ProgressBar

        // Listener untuk tombol Register
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Listener untuk teks "Log in here"
        txtLoginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void registerUser() {
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
                            Toast.makeText(RegisterActivity.this, "NIK sudah terdaftar.", Toast.LENGTH_LONG).show();
                        } else {
                            // NIK unik, lanjutkan pendaftaran (ProgressBar tetap terlihat)
                            createFirebaseUser(email, password, nama, nik, telepon);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Jika gagal, sembunyikan ProgressBar dan tampilkan pesan error
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createFirebaseUser(String email, String password, final String nama, final String nik, final String telepon) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // KIRIM EMAIL VERIFIKASI
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "Pendaftaran berhasil. Silakan cek email Anda untuk verifikasi.",
                                                            Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "Gagal mengirim email verifikasi.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                // Lanjutkan untuk menyimpan data pengguna
                                String userId = user.getUid();
                                saveUserDataToDatabase(userId, nama, nik, telepon, email);
                            }
                        } else {
                            // Jika gagal, sembunyikan ProgressBar dan tampilkan pesan error
                            progressBar.setVisibility(View.GONE);
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(RegisterActivity.this, "Email sudah terdaftar.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(RegisterActivity.this, "Autentikasi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void saveUserDataToDatabase(String userId, String nama, String nik, String telepon, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("namaLengkap", nama);
        userData.put("nik", nik);
        userData.put("nomorTelepon", telepon);
        userData.put("email", email);
        userData.put("uid", userId);

        databaseReference.child(userId).setValue(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Jika berhasil, sembunyikan ProgressBar sebelum pindah activity
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Data pengguna berhasil disimpan di Realtime Database!");
                        Toast.makeText(RegisterActivity.this, "Pendaftaran berhasil! Silakan login.", Toast.LENGTH_LONG).show();

                        // *** PERUBAHAN DI SINI ***
                        // Logout pengguna agar harus login manual setelah registrasi
                        mAuth.signOut();

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Jika gagal, sembunyikan ProgressBar dan tampilkan pesan error
                        progressBar.setVisibility(View.GONE);
                        Log.w(TAG, "Gagal menyimpan data pengguna", e);
                        Toast.makeText(RegisterActivity.this, "Gagal menyimpan data pengguna.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
