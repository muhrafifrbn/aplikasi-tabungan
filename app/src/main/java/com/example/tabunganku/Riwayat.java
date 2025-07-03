package com.example.tabunganku;

public class Riwayat {
    private String jenis;
    private String keterangan;
    private long jumlah;
    private long timestamp;

    public Riwayat() {}

    public Riwayat(String jenis, String keterangan, long jumlah, long timestamp) {
        this.jenis = jenis;
        this.keterangan = keterangan;
        this.jumlah = jumlah;
        this.timestamp = timestamp;
    }

    // Getters
    public String getJenis() { return jenis; }
    public String getKeterangan() { return keterangan; }
    public long getJumlah() { return jumlah; }
    public long getTimestamp() { return timestamp; }
}