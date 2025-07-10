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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText inputNama, inputNIK, inputTelepon, inputEmail;
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


        // Tampilkan ProgressBar sebelum memulai proses
        progressBar.setVisibility(View.VISIBLE);

        // Mendapatkan UID pengguna yang sedang login
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Ambil NIK pengguna yang sudah ada di database untuk membandingkan dengan NIK baru
        databaseReference.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                String currentNik = dataSnapshot.child("nik").getValue(String.class);

                // Periksa apakah NIK yang dimasukkan berbeda dengan NIK yang sudah ada
                if (currentNik.equals(nik)) {
                    // Jika NIK tidak berubah, langsung update data pengguna tanpa validasi NIK
//                    updateUserDataInDatabase(uid, nama, nik, telepon, email);
                    // NIK unik, lanjutkan update data pengguna
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    // Memeriksa apakah email baru sama dengan email yang ada
                    if (currentUser != null && email.equals(currentUser.getEmail())) {
                        // Jika email tidak berubah, hanya update data lainnya di Realtime Database
                        updateUserDataInDatabase(uid, nama, nik, telepon, email);
                    } else {
                        // Jika email berubah, update email di Firebase Authentication
                        updateEmailInAuthentication(currentUser, uid, nama, nik, telepon, email);
//                        testerUpdateEmail(email);
                        Log.d("statu-email", "Berubah");
                    }

                } else {
                    // Jika NIK berbeda, lakukan validasi NIK
                    validateAndUpdateNIK(uid, nama, nik, telepon, email);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Gagal mengambil data pengguna.", Toast.LENGTH_LONG).show();
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

//    private void updateUserToFirebase(String nama, String nik, String email, String telepon){
//        // NIK unik, lanjutkan update data pengguna
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        // Membuat Map untuk data yang akan diupdate
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("namaLengkap", nama);
//        userData.put("nik", nik);
//        userData.put("nomorTelepon", telepon);
//        userData.put("email", email);
//        userData.put("uid", uid);
//
//         // Update data pengguna di Realtime Database
//        databaseReference.child(uid).updateChildren(userData)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Setelah berhasil update di Realtime Database, update email di Firebase Authentication
//                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                        if (user != null) {
//                            user.updateEmail(email).addOnCompleteListener(updateTask -> {
//                                if (updateTask.isSuccessful()) {
//                                    // Kirimkan email verifikasi
//                                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
//                                        if (verificationTask.isSuccessful()) {
//                                            progressBar.setVisibility(View.GONE);
//                                            Toast.makeText(getApplicationContext(), "Email berhasil diperbarui. Cek email Anda untuk verifikasi.", Toast.LENGTH_SHORT).show();
//                                        } else {
//                                            progressBar.setVisibility(View.GONE);
//                                            Toast.makeText(getApplicationContext(), "Gagal mengirim email verifikasi.", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                } else {
//                                    progressBar.setVisibility(View.GONE);
//                                    Toast.makeText(getApplicationContext(), "Gagal memperbarui email di Authentication.", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    } else {
//                        progressBar.setVisibility(View.GONE);
//                        Toast.makeText(getApplicationContext(), "Gagal memperbarui data.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


    // Fungsi untuk memvalidasi NIK dan memperbarui data pengguna jika NIK baru unik
    private void validateAndUpdateNIK(String uid, String nama, String nik, String telepon, String email) {
        // Cek apakah NIK baru sudah terdaftar di Realtime Database
        databaseReference.orderByChild("nik").equalTo(nik)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Jika NIK sudah ada, sembunyikan ProgressBar dan tampilkan pesan
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "NIK sudah terdaftar.", Toast.LENGTH_LONG).show();
                        } else {
                            // NIK unik, lanjutkan update data pengguna
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            // Memeriksa apakah email baru sama dengan email yang ada
                            if (currentUser != null && email.equals(currentUser.getEmail())) {
                                // Jika email tidak berubah, hanya update data lainnya di Realtime Database
                                updateUserDataInDatabase(uid, nama, nik, telepon, email);
                            } else {
                                // Jika email berubah, update email di Firebase Authentication
                                updateEmailInAuthentication(currentUser, uid, nama, nik, telepon, email);
                            }
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


    // Fungsi untuk memperbarui data pengguna di Realtime Database
    private void updateUserDataInDatabase(String uid, String nama, String nik, String telepon, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("namaLengkap", nama);
        userData.put("nik", nik);
        userData.put("nomorTelepon", telepon);
        userData.put("email", email);
        userData.put("uid", uid);

        // Update data pengguna di Realtime Database
        databaseReference.child(uid).updateChildren(userData)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Data berhasil diperbarui.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Gagal memperbarui data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Fungsi untuk memperbarui email di Firebase Authentication dan mengirimkan email verifikasi
    private void updateEmailInAuthentication(FirebaseUser currentUser, String uid, String nama, String nik, String telepon, String email) {
        // Membuat kredensial untuk autentikasi ulang
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), "1234567");
        FirebaseUser userLogin = FirebaseAuth.getInstance().getCurrentUser();

        // Lakukan autentikasi ulang pengguna dengan kredensial
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Jika autentikasi ulang berhasil, perbarui email
                userLogin.updateEmail(email).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        // Kirimkan email verifikasi setelah memperbarui email
                        currentUser.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                            if (verificationTask.isSuccessful()) {
                                // Setelah berhasil memperbarui email dan mengirimkan email verifikasi, update data di Realtime Database
                                updateUserDataInDatabase(uid, nama, nik, telepon, email);

                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Email berhasil diperbarui. Cek email Anda untuk verifikasi.", Toast.LENGTH_SHORT).show();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Log.e("UpdateEmail", "Gagal mengirim email verifikasi: " + verificationTask.getException().getMessage());
                                Toast.makeText(getApplicationContext(), "Gagal mengirim email verifikasi.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Log.e("UpdateEmail", "Gagal memperbarui email di Authentication: " + updateTask.getException().getMessage());
                        Log.e("UpdateEmail", "Error detail: ", updateTask.getException());
                        Log.e("UpdateEmail", currentUser.getEmail());
                        Toast.makeText(getApplicationContext(), "Gagal memperbarui email di Authentication.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Log.e("UpdateEmail", "Gagal melakukan autentikasi ulang: " + task.getException().getMessage());
                Toast.makeText(getApplicationContext(), "Autentikasi ulang gagal. Periksa kata sandi Anda.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void testerUpdateEmail(String newEmail) {
        FirebaseUser userLogin = FirebaseAuth.getInstance().getCurrentUser();

        if (userLogin == null) {
            Toast.makeText(getApplicationContext(), "User belum login.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!userLogin.isEmailVerified()) {
            Toast.makeText(getApplicationContext(), "Email Anda belum diverifikasi. Silakan verifikasi dulu sebelum mengganti email.", Toast.LENGTH_LONG).show();
            userLogin.sendEmailVerification(); // opsional: kirim ulang verifikasi
            return;
        }

        // Membuat kredensial untuk autentikasi ulang
        AuthCredential credential = EmailAuthProvider.getCredential(userLogin.getEmail(), "1234567");  // Masukkan password yang sesuai

        // Lakukan autentikasi ulang pengguna dengan kredensial
        userLogin.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Jika autentikasi ulang berhasil, perbarui email ke email baru
                userLogin.updateEmail("lilincemerlang3@gmail.com").addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        // Kirim email verifikasi ke email baru
                        userLogin.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                            if (verifyTask.isSuccessful()) {
                                Log.e("UpdateEmail", "Email berhasil diperbarui dan verifikasi dikirim.");
                                Toast.makeText(getApplicationContext(), "Email berhasil diperbarui. Cek email Anda untuk verifikasi.", Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("UpdateEmail", "Gagal mengirim email verifikasi: " + verifyTask.getException().getMessage());
                                Toast.makeText(getApplicationContext(), "Gagal mengirim email verifikasi ke alamat baru.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e("UpdateEmail", "Gagal memperbarui email di Authentication: " + updateTask.getException().getMessage());
                        Log.e("UpdateEmail", "Error detail: ", updateTask.getException());
                        Toast.makeText(getApplicationContext(), "Gagal memperbarui email di Authentication.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("UpdateEmail", "Gagal melakukan autentikasi ulang: " + task.getException().getMessage());
                Toast.makeText(getApplicationContext(), "Autentikasi ulang gagal. Periksa kata sandi Anda.", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    private void createFirebaseUser(String email) {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            if (user != null) {
//                                // KIRIM EMAIL VERIFIKASI
//                                user.sendEmailVerification()
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//                                                    Toast.makeText(RegisterActivity.this,
//                                                            "Pendaftaran berhasil. Silakan cek email Anda untuk verifikasi.",
//                                                            Toast.LENGTH_LONG).show();
//                                                } else {
//                                                    Toast.makeText(RegisterActivity.this,
//                                                            "Gagal mengirim email verifikasi.",
//                                                            Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        })
//                            }
//                        } else {
//                            // Jika gagal, sembunyikan ProgressBar dan tampilkan pesan error
//                            progressBar.setVisibility(View.GONE);
//                            Log.w("gagalEmail", "createUserWithEmail:failure", task.getException());
//
//                        }
//                    }
//                });
//    }
}