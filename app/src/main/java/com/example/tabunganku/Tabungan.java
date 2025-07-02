package com.example.tabunganku;

public class Tabungan {
    private String id; // Field baru untuk menyimpan ID unik dari Firebase
    private String nama;
    private long target;
    private long terkumpul;

    // Penting: butuh konstruktor kosong untuk Firebase
    public Tabungan() {}

    public Tabungan(String nama, long target, long terkumpul) {
        this.nama = nama;
        this.target = target;
        this.terkumpul = terkumpul;
    }

    // Getter dan Setter untuk ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Getter lainnya tetap sama
    public String getNama() { return nama; }
    public long getTarget() { return target; }
    public long getTerkumpul() { return terkumpul; }
}