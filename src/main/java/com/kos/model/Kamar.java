package com.kos.model;

import java.time.LocalDate;

public abstract class Kamar implements InfoDetail {
    private final String nomorKamar;
    private double hargaDasar;
    private boolean terisi;
    private Penyewa penyewaAktif;
    private LocalDate tanggalJatuhTempo;
    private Tagihan tagihanAktif;

    public Kamar(String nomorKamar, double hargaDasar) {
        this.nomorKamar = nomorKamar;
        this.hargaDasar = hargaDasar;
        this.terisi = false;
        this.penyewaAktif = null;
        this.tanggalJatuhTempo = null;
        this.tagihanAktif = null;
    }

    public abstract String getJenisKamar();

    public abstract double getTarifPerBulan();

    public abstract double hitungHargaTotal(int jumlahBulan);

    public void tempati(Penyewa penyewa, LocalDate jatuhTempo) {
        if (terisi) {
            throw new IllegalStateException("Kamar " + nomorKamar + " sudah terisi.");
        }
        this.penyewaAktif = penyewa;
        this.tanggalJatuhTempo = jatuhTempo;
        this.terisi = true;
    }

    public Penyewa kosongkan() {
        if (!terisi) {
            throw new IllegalStateException("Kamar " + nomorKamar + " sudah kosong.");
        }
        Penyewa lama = penyewaAktif;
        this.penyewaAktif = null;
        this.tanggalJatuhTempo = null;
        this.tagihanAktif = null;
        this.terisi = false;
        return lama;
    }

    public StatusJatuhTempo getStatusJatuhTempo(LocalDate hariIni) {
        if (!terisi) {
            return null;
        }
        return StatusJatuhTempo.hitung(tanggalJatuhTempo, hariIni);
    }

    public boolean isBelumLunas() {
        return terisi && tagihanAktif != null && !tagihanAktif.isLunas();
    }

    public Tagihan getTagihanAktif() {
        return tagihanAktif;
    }

    public void setTagihanAktif(Tagihan tagihanAktif) {
        this.tagihanAktif = tagihanAktif;
    }

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

    protected static void periksaJumlahBulan(int jumlahBulan) {
        if (jumlahBulan < 1) {
            throw new IllegalArgumentException("Durasi sewa minimal 1 bulan.");
        }
    }
}
