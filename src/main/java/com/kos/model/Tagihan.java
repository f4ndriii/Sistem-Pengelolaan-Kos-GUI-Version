package com.kos.model;

public class Tagihan {
    private final int jumlahBulan;
    private final double total;
    private double terbayar;

    public Tagihan(int jumlahBulan, double total) {
        this.jumlahBulan = jumlahBulan;
        this.total = total;
    }

    public int getJumlahBulan() {
        return jumlahBulan;
    }

    public double getTotal() {
        return total;
    }

    public double getTerbayar() {
        return terbayar;
    }

    public double getSisa() {
        return Math.max(total - terbayar, 0);
    }

    public boolean isLunas() {
        return terbayar >= total;
    }

    public void tambahBayar(double nominal) {
        terbayar += nominal;
    }
}
