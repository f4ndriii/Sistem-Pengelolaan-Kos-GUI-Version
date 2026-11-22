/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kos.model;

/**
 *
 * @author USER
 */
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
        if (jumlahBulan <= 0) {
            return 0;
        }
        return getTarifPerBulan() * jumlahBulan;
    }

    @Override
    public String getRingkasanDetail() {
        String status = isTerisi() ? "Terisi (" + getPenyewaAktif().getNama() + ")" : "Kosong";
        return "Kamar " + getNomorKamar() + " [" + getJenisKamar() + "] - Status: " + status
                + " - Tarif/bln: Rp" + String.format("%,.0f", getTarifPerBulan());
    }
}