/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kos.model;

import java.time.LocalDate;

/**
 *
 * @author USER
 */
public class Penyewa implements InfoDetail {
    private final String idPenyewa;
    private String nama;
    private String kontak;
    private Kamar kamarSewa;
    private LocalDate tanggalMasuk;

    public Penyewa(String idPenyewa, String nama, String kontak, Kamar kamarSewa, LocalDate tanggalMasuk) {
        this.idPenyewa = idPenyewa;
        this.nama = nama;
        this.kontak = kontak;
        this.kamarSewa = kamarSewa;
        this.tanggalMasuk = tanggalMasuk;
    }

    public String getIdPenyewa() {
        return idPenyewa;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getKontak() {
        return kontak;
    }

    public void setKontak(String kontak) {
        this.kontak = kontak;
    }

    public Kamar getKamarSewa() {
        return kamarSewa;
    }

    public void setKamarSewa(Kamar kamarSewa) {
        this.kamarSewa = kamarSewa;
    }

    public LocalDate getTanggalMasuk() {
        return tanggalMasuk;
    }

    public void setTanggalMasuk(LocalDate tanggalMasuk) {
        this.tanggalMasuk = tanggalMasuk;
    }

    @Override
    public String getRingkasanDetail() {
        String noKamar = (kamarSewa != null) ? kamarSewa.getNomorKamar() : "-";
        return nama + " (ID: " + idPenyewa + ") - Kamar: " + noKamar + " - Kontak: " + kontak;
    }
}
