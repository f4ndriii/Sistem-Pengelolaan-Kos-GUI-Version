package com.kos.model;

public class KamarPremium extends Kamar {
    private double biayaFasilitas;

    public KamarPremium(String nomorKamar, double hargaDasar, double biayaFasilitas) {
        super(nomorKamar, hargaDasar);
        this.biayaFasilitas = biayaFasilitas;
    }

    public double getBiayaFasilitas() {
        return biayaFasilitas;
    }

    public void setBiayaFasilitas(double biayaFasilitas) {
        this.biayaFasilitas = biayaFasilitas;
    }

    @Override
    public String getJenisKamar() {
        return "Premium";
    }

    @Override
    public double getTarifPerBulan() {
        return getHargaDasar() + biayaFasilitas;
    }

    @Override
    public double hitungHargaTotal(int jumlahBulan) {
        periksaJumlahBulan(jumlahBulan);
        return getTarifPerBulan() * jumlahBulan;
    }

    @Override
    public String getInfoDetail() {
        return "Kamar " + getNomorKamar() + " (Premium) - tarif Rp"
                + String.format("%,.0f", getTarifPerBulan()) + "/bulan (termasuk fasilitas Rp"
                + String.format("%,.0f", biayaFasilitas) + ")";
    }
}
