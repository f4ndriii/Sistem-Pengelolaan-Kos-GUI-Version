package com.kos.model;

import java.time.LocalDate;

public class Pembayaran implements InfoDetail {
    private final Penyewa penyewa;
    private final Kamar kamar;
    private final int jumlahBulan;
    private final double nominal;
    private final LocalDate tanggalBayar;
    private final LocalDate jatuhTempoBerikutnya;
    private final double sisaSetelahBayar;

    public Pembayaran(Penyewa penyewa, Kamar kamar, int jumlahBulan, double nominal,
            LocalDate tanggalBayar, LocalDate jatuhTempoBerikutnya, double sisaSetelahBayar) {
        this.penyewa = penyewa;
        this.kamar = kamar;
        this.jumlahBulan = jumlahBulan;
        this.nominal = nominal;
        this.tanggalBayar = tanggalBayar;
        this.jatuhTempoBerikutnya = jatuhTempoBerikutnya;
        this.sisaSetelahBayar = sisaSetelahBayar;
    }

    public Penyewa getPenyewa() {
        return penyewa;
    }

    public Kamar getKamar() {
        return kamar;
    }

    public int getJumlahBulan() {
        return jumlahBulan;
    }

    public double getNominal() {
        return nominal;
    }

    public LocalDate getTanggalBayar() {
        return tanggalBayar;
    }

    public LocalDate getJatuhTempoBerikutnya() {
        return jatuhTempoBerikutnya;
    }

    /** Sisa tagihan yang masih harus dibayar setelah pembayaran ini (0 = lunas). */
    public double getSisaSetelahBayar() {
        return sisaSetelahBayar;
    }

    public boolean isLunas() {
        return sisaSetelahBayar <= 0;
    }

    @Override
    public String getInfoDetail() {
        return "Pembayaran " + jumlahBulan + " bulan oleh " + penyewa.getNama()
                + " untuk kamar " + kamar.getNomorKamar() + " pada " + tanggalBayar
                + " sebesar Rp" + String.format("%,.0f", nominal)
                + (isLunas() ? " (lunas), jatuh tempo berikutnya " + jatuhTempoBerikutnya
                : " (belum lunas, sisa Rp" + String.format("%,.0f", sisaSetelahBayar) + ")");
    }
}
