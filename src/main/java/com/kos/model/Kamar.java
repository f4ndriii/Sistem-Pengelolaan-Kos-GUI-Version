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
public abstract class Kamar implements InfoDetail {
    private final String nomorKamar;
    private double hargaDasar;
    private boolean terisi;
    private Penyewa penyewaAktif;
    private LocalDate tanggalJatuhTempo;

    public Kamar(String nomorKamar, double hargaDasar) {
        this.nomorKamar = nomorKamar;
        this.hargaDasar = hargaDasar;
        this.terisi = false;
        this.penyewaAktif = null;
        this.tanggalJatuhTempo = null;
    }

    public abstract String getJenisKamar();

    public abstract double getTarifPerBulan();

    public abstract double hitungHargaTotal(int jumlahBulan);

    public String getNomorKamar() {
        return nomorKamar;
    }

    public double getHargaDasar() {
        return hargaDasar;
    }

    public void setHargaDasar(double hargaDasar) {
        this.hargaDasar = hargaDasar;
    }

    public boolean isTerisi() {
        return terisi;
    }

    public void setTerisi(boolean terisi) {
        this.terisi = terisi;
    }

    public Penyewa getPenyewaAktif() {
        return penyewaAktif;
    }

    public void setPenyewaAktif(Penyewa penyewaAktif) {
        this.penyewaAktif = penyewaAktif;
    }

    public LocalDate getTanggalJatuhTempo() {
        return tanggalJatuhTempo;
    }

    public void setTanggalJatuhTempo(LocalDate tanggalJatuhTempo) {
        this.tanggalJatuhTempo = tanggalJatuhTempo;
    }
}
