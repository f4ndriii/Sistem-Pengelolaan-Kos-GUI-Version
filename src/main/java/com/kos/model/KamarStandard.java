package com.kos.model;

public class KamarStandard extends Kamar {

    public KamarStandard(String nomorKamar, double hargaDasar) {
        super(nomorKamar, hargaDasar);
    }

    @Override
    public String getJenisKamar() {
        return "Standard";
    }

    @Override
    public double getTarifPerBulan() {
        return getHargaDasar();
    }

    @Override
    public double hitungHargaTotal(int jumlahBulan) {
        periksaJumlahBulan(jumlahBulan);
        return getTarifPerBulan() * jumlahBulan;
    }

    @Override
    public String getInfoDetail() {
        return "Kamar " + getNomorKamar() + " (Standard) - tarif Rp"
                + String.format("%,.0f", getTarifPerBulan()) + "/bulan";
    }
}
