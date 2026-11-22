package com.kos.model;

import java.time.LocalDate;

public class Penyewa implements InfoDetail {
    private final String nik;
    private final String nama;
    private final String kontak;
    private final Kamar kamar;
    private final LocalDate tanggalMasuk;
    private LocalDate tanggalKeluar;

    public Penyewa(String nik, String nama, String kontak, Kamar kamar, LocalDate tanggalMasuk) {
        this.nik = nik;
        this.nama = nama;
        this.kontak = kontak;
        this.kamar = kamar;
        this.tanggalMasuk = tanggalMasuk;
    }

    public String getNik() {
        return nik;
    }

    public String getNama() {
        return nama;
    }

    public String getKontak() {
        return kontak;
    }

    public Kamar getKamar() {
        return kamar;
    }

    public LocalDate getTanggalMasuk() {
        return tanggalMasuk;
    }

    public LocalDate getTanggalKeluar() {
        return tanggalKeluar;
    }

    public boolean isAktif() {
        return tanggalKeluar == null;
    }

    public void keluar(LocalDate tanggal) {
        this.tanggalKeluar = tanggal;
    }

    @Override
    public String getInfoDetail() {
        return nama + " (ID/NIK " + nik + ", kontak " + kontak + ")";
    }

    @Override
    public String toString() {
        return nama;
    }
}
