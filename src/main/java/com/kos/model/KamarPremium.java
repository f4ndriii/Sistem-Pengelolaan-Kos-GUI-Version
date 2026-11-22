/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kos.model;

/**
 *
 * @author USER
 */
public class KamarPremium extends Kamar {
    private double biayaFasilitasTambahan;

    public KamarPremium(String nomorKamar, double hargaDasar, double biayaFasilitasTambahan) {
        super(nomorKamar, hargaDasar);
        this.biayaFasilitasTambahan = biayaFasilitasTambahan;
    }

    public double getBiayaFasilitasTambahan() {
        return biayaFasilitasTambahan;
    }

    public void setBiayaFasilitasTambahan(double biayaFasilitasTambahan) {
        this.biayaFasilitasTambahan = biayaFasilitasTambahan;
    }

    @Override
    public String getJenisKamar() {
        return "Premium";
    }

    @Override
    public double getTarifPerBulan() {
        return getHargaDasar() + biayaFasilitasTambahan;
    }

    @Override
    public double hitungHargaTotal(int jumlahBulan) {
        if (jumlahBulan <= 0) {
            return 0;
        }
        return getTarifPerBulan() * jumlahBulan;
    }

    @Override
    public String getRingkasanDetail() {
        String status = isTerisi() ? "Terisi (" + getPenyewaAktif().getNama() + ")" : "Kosong";
        return "Kamar " + getNomorKamar() + " [" + getJenisKamar() + "] - Status: " + status
                + " - Tarif/bln: Rp" + String.format("%,.0f", getTarifPerBulan())
                + " (Dasar: Rp" + String.format("%,.0f", getHargaDasar())
                + " + Fasilitas: Rp" + String.format("%,.0f", biayaFasilitasTambahan) + ")";
    }
}
