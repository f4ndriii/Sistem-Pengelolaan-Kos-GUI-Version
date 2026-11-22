package com.kos.manager;

import com.kos.model.Kamar;
import com.kos.model.KamarPremium;
import com.kos.model.ItemSampah;
import com.kos.model.KamarStandard;
import com.kos.model.Pembayaran;
import com.kos.model.Penyewa;
import com.kos.model.StatusJatuhTempo;
import com.kos.model.Tagihan;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class KosManager {

    public enum UrutanKamar {
        NOMOR_KAMAR("Nomor Kamar"),
        JENIS_KAMAR("Jenis Kamar (Standard/Premium)"),
        STATUS("Status Kosong/Terisi"),
        JATUH_TEMPO("Jatuh Tempo Terdekat");

        private final String label;

        UrutanKamar(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final LinkedList<Kamar> daftarKamar = new LinkedList<>();
    private final LinkedList<Penyewa> daftarPenyewa = new LinkedList<>();
    private final LinkedList<Pembayaran> daftarPembayaran = new LinkedList<>();
    private final LinkedList<ItemSampah> sampahKamar = new LinkedList<>();

    private Runnable pendengarPerubahan;

    public void setPendengarPerubahan(Runnable pendengar) {
        this.pendengarPerubahan = pendengar;
    }

    public boolean isPenyimpananAktif() {
        return pendengarPerubahan != null;
    }

    public void simpanSekarang() {
        perubahan();
    }

    private void perubahan() {
        if (pendengarPerubahan != null) {
            pendengarPerubahan.run();
        }
    }

    public List<Kamar> getSemuaKamar() {
        return new ArrayList<>(daftarKamar);
    }

    public List<Penyewa> getSemuaPenyewa() {
        return new ArrayList<>(daftarPenyewa);
    }

    public List<Pembayaran> getSemuaPembayaran() {
        return new ArrayList<>(daftarPembayaran);
    }

    public void muatData(List<Kamar> kamar, List<Penyewa> penyewa, List<Pembayaran> pembayaran,
            List<ItemSampah> sampah) {
        daftarKamar.clear();
        daftarKamar.addAll(kamar);
        daftarPenyewa.clear();
        daftarPenyewa.addAll(penyewa);
        daftarPembayaran.clear();
        daftarPembayaran.addAll(pembayaran);
        sampahKamar.clear();
        sampahKamar.addAll(sampah);
    }


    public Kamar tambahKamarStandard(String nomorKamar, double hargaDasar) {
        String nomor = validasiKamarBaru(nomorKamar, hargaDasar);
        Kamar kamar = new KamarStandard(nomor, hargaDasar);
        daftarKamar.add(kamar);
        perubahan();
        return kamar;
    }

    public Kamar tambahKamarPremium(String nomorKamar, double hargaDasar, double biayaFasilitas) {
        String nomor = validasiKamarBaru(nomorKamar, hargaDasar);
        if (biayaFasilitas < 0) {
            throw new IllegalArgumentException("Biaya fasilitas tidak boleh negatif.");
        }
        Kamar kamar = new KamarPremium(nomor, hargaDasar, biayaFasilitas);
        daftarKamar.add(kamar);
        perubahan();
        return kamar;
    }

    private String validasiKamarBaru(String nomorKamar, double hargaDasar) {
        String nomor = nomorKamar == null ? "" : nomorKamar.trim();
        if (nomor.isEmpty()) {
            throw new IllegalArgumentException("Nomor kamar wajib diisi.");
        }
        if (nomor.length() > 10) {
            throw new IllegalArgumentException("Nomor kamar maksimal 10 karakter.");
        }
        if (cariKamar(nomor) != null) {
            throw new IllegalArgumentException("Nomor kamar " + nomor + " sudah ada.");
        }
        if (hargaDasar <= 0) {
            throw new IllegalArgumentException("Tarif dasar harus lebih dari 0.");
        }
        return nomor;
    }

    public void hapusKamar(String nomorKamar) {
        Kamar kamar = ambilKamar(nomorKamar);
        if (kamar.isTerisi()) {
            throw new IllegalStateException("Kamar " + kamar.getNomorKamar() + " masih terisi oleh "
                    + kamar.getPenyewaAktif().getNama() + ". Kosongkan kamar terlebih dahulu.");
        }
        daftarKamar.remove(kamar);
        sampahKamar.addFirst(new ItemSampah(kamar, LocalDateTime.now()));
        perubahan();
    }

    public List<ItemSampah> getSampah() {
        return new ArrayList<>(sampahKamar);
    }

    public int getJumlahSampah() {
        return sampahKamar.size();
    }

    public Kamar pulihkanKamar(Kamar kamar) {
        ItemSampah item = cariDiSampah(kamar);
        if (cariKamar(kamar.getNomorKamar()) != null) {
            throw new IllegalStateException("Nomor kamar " + kamar.getNomorKamar()
                    + " sudah dipakai kamar lain. Hapus kamar tersebut atau hapus permanen item ini.");
        }
        sampahKamar.remove(item);
        daftarKamar.add(kamar);
        perubahan();
        return kamar;
    }

    public void hapusPermanen(Kamar kamar) {
        sampahKamar.remove(cariDiSampah(kamar));
        perubahan();
    }

    public void kosongkanSampah() {
        sampahKamar.clear();
        perubahan();
    }

    private ItemSampah cariDiSampah(Kamar kamar) {
        for (ItemSampah item : sampahKamar) {
            if (item.getKamar() == kamar) {
                return item;
            }
        }
        throw new IllegalArgumentException("Kamar tidak ada di Sampah.");
    }

    public Penyewa tambahPenyewa(String nomorKamar, String nik, String nama, String kontak,
            LocalDate tanggalMasuk, int durasiBulan, double nominalDibayar) {
        Kamar kamar = ambilKamar(nomorKamar);
        if (kamar.isTerisi()) {
            throw new IllegalStateException("Kamar " + kamar.getNomorKamar() + " sudah terisi oleh "
                    + kamar.getPenyewaAktif().getNama() + ". Kosongkan kamar terlebih dahulu.");
        }
        String nikBersih = nik == null ? "" : nik.trim();
        String namaBersih = nama == null ? "" : nama.trim();
        String kontakBersih = kontak == null ? "" : kontak.trim();

        if (nikBersih.isEmpty()) {
            throw new IllegalArgumentException("ID/NIK wajib diisi.");
        }
        if (nikBersih.length() > 30) {
            throw new IllegalArgumentException("ID/NIK maksimal 30 karakter.");
        }
        if (namaBersih.isEmpty()) {
            throw new IllegalArgumentException("Nama penyewa wajib diisi.");
        }
        if (!kontakBersih.matches("[0-9+\\-\\s()]{6,20}")) {
            throw new IllegalArgumentException(
                    "Kontak tidak valid. Isi nomor telepon (6-20 karakter: angka, +, -, spasi).");
        }
        if (tanggalMasuk == null) {
            throw new IllegalArgumentException("Tanggal masuk wajib diisi.");
        }
        if (durasiBulan < 1) {
            throw new IllegalArgumentException("Durasi sewa minimal 1 bulan.");
        }
        double totalBiaya = kamar.hitungHargaTotal(durasiBulan);
        if (nominalDibayar < 0) {
            throw new IllegalArgumentException("Nominal pembayaran tidak boleh negatif.");
        }
        if (nominalDibayar > totalBiaya) {
            throw new IllegalArgumentException("Nominal melebihi total biaya sewa ("
                    + String.format("%,.0f", totalBiaya) + ").");
        }
        for (Penyewa p : daftarPenyewa) {
            if (p.isAktif() && p.getNik().equalsIgnoreCase(nikBersih)) {
                throw new IllegalArgumentException("ID/NIK " + nikBersih
                        + " sudah tercatat sebagai penyewa aktif di kamar "
                        + p.getKamar().getNomorKamar() + ".");
            }
        }

        Penyewa penyewa = new Penyewa(nikBersih, namaBersih, kontakBersih, kamar, tanggalMasuk);
        boolean lunas = nominalDibayar >= totalBiaya;
        LocalDate jatuhTempo = lunas ? tanggalMasuk.plusMonths(durasiBulan) : tanggalMasuk;
        kamar.tempati(penyewa, jatuhTempo);
        daftarPenyewa.add(penyewa);

        if (nominalDibayar > 0) {
            Tagihan tagihan = new Tagihan(durasiBulan, totalBiaya);
            tagihan.tambahBayar(nominalDibayar);
            if (!lunas) {
                kamar.setTagihanAktif(tagihan);
            }
            daftarPembayaran.add(new Pembayaran(penyewa, kamar, durasiBulan, nominalDibayar,
                    tanggalMasuk, jatuhTempo, tagihan.getSisa()));
        }
        perubahan();
        return penyewa;
    }

    public Penyewa kosongkanKamar(String nomorKamar) {
        Kamar kamar = ambilKamar(nomorKamar);
        if (!kamar.isTerisi()) {
            throw new IllegalStateException("Kamar " + kamar.getNomorKamar() + " sudah kosong.");
        }
        Penyewa penyewa = kamar.kosongkan();
        penyewa.keluar(LocalDate.now());
        perubahan();
        return penyewa;
    }

 
    public double hitungBiayaSewa(String nomorKamar, int jumlahBulan) {
        return ambilKamar(nomorKamar).hitungHargaTotal(jumlahBulan);
    }

    public Pembayaran catatPembayaran(String nomorKamar, int jumlahBulan, double nominal,
            LocalDate tanggalBayar) {
        Kamar kamar = ambilKamar(nomorKamar);
        if (!kamar.isTerisi()) {
            throw new IllegalStateException(
                    "Kamar " + kamar.getNomorKamar() + " kosong, tidak ada penyewa yang membayar.");
        }
        if (kamar.isBelumLunas()) {
            throw new IllegalStateException("Kamar " + kamar.getNomorKamar()
                    + " masih punya tagihan belum lunas (sisa "
                    + String.format("%,.0f", kamar.getTagihanAktif().getSisa())
                    + "). Lunasi tagihan tersebut terlebih dahulu.");
        }
        if (jumlahBulan < 1) {
            throw new IllegalArgumentException("Jumlah bulan yang dibayar minimal 1.");
        }
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal pembayaran harus lebih dari 0.");
        }
        if (tanggalBayar == null) {
            throw new IllegalArgumentException("Tanggal bayar wajib diisi.");
        }
        double total = kamar.hitungHargaTotal(jumlahBulan);
        if (nominal > total) {
            throw new IllegalArgumentException("Nominal melebihi total tagihan "
                    + jumlahBulan + " bulan (" + String.format("%,.0f", total) + ").");
        }
        Tagihan tagihan = new Tagihan(jumlahBulan, total);
        tagihan.tambahBayar(nominal);
        return selesaikanPembayaran(kamar, tagihan, nominal, tanggalBayar);
    }

    public Pembayaran lunasiTagihan(String nomorKamar, double nominal, LocalDate tanggalBayar) {
        Kamar kamar = ambilKamar(nomorKamar);
        if (!kamar.isBelumLunas()) {
            throw new IllegalStateException(
                    "Kamar " + kamar.getNomorKamar() + " tidak punya tagihan yang belum lunas.");
        }
        Tagihan tagihan = kamar.getTagihanAktif();
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal pembayaran harus lebih dari 0.");
        }
        if (tanggalBayar == null) {
            throw new IllegalArgumentException("Tanggal bayar wajib diisi.");
        }
        if (nominal > tagihan.getSisa()) {
            throw new IllegalArgumentException("Nominal melebihi sisa tagihan ("
                    + String.format("%,.0f", tagihan.getSisa()) + ").");
        }
        tagihan.tambahBayar(nominal);
        return selesaikanPembayaran(kamar, tagihan, nominal, tanggalBayar);
    }

    private Pembayaran selesaikanPembayaran(Kamar kamar, Tagihan tagihan, double nominal,
            LocalDate tanggalBayar) {
        if (tagihan.isLunas()) {
            kamar.setTanggalJatuhTempo(
                    kamar.getTanggalJatuhTempo().plusMonths(tagihan.getJumlahBulan()));
            kamar.setTagihanAktif(null);
        } else {
            kamar.setTagihanAktif(tagihan);
        }
        Pembayaran pembayaran = new Pembayaran(kamar.getPenyewaAktif(), kamar,
                tagihan.getJumlahBulan(), nominal, tanggalBayar, kamar.getTanggalJatuhTempo(),
                tagihan.getSisa());
        daftarPembayaran.add(pembayaran);
        perubahan();
        return pembayaran;
    }

    public List<Pembayaran> getRiwayatPembayaran(Penyewa penyewa) {
        List<Pembayaran> hasil = new ArrayList<>();
        for (Pembayaran p : daftarPembayaran) {
            if (p.getPenyewa() == penyewa) {
                hasil.add(p);
            }
        }
        return hasil;
    }

    public List<Kamar> getDaftarKamar(String kataKunci, UrutanKamar urutan) {
        String kunci = kataKunci == null ? "" : kataKunci.trim().toLowerCase(Locale.ROOT);
        List<Kamar> hasil = new ArrayList<>();
        for (Kamar k : daftarKamar) {
            if (kunci.isEmpty() || cocok(k, kunci)) {
                hasil.add(k);
            }
        }
        hasil.sort(pembanding(urutan == null ? UrutanKamar.NOMOR_KAMAR : urutan));
        return hasil;
    }

    private boolean cocok(Kamar kamar, String kunci) {
        if (kamar.getNomorKamar().toLowerCase(Locale.ROOT).contains(kunci)) {
            return true;
        }
        return kamar.isTerisi()
                && kamar.getPenyewaAktif().getNama().toLowerCase(Locale.ROOT).contains(kunci);
    }

    private Comparator<Kamar> pembanding(UrutanKamar urutan) {
        Comparator<Kamar> berdasarkanNomor = (a, b) ->
                bandingkanNomor(a.getNomorKamar(), b.getNomorKamar());
        switch (urutan) {
            case JENIS_KAMAR:
                return Comparator.<Kamar>comparingInt(k -> k instanceof KamarPremium ? 1 : 0)
                        .thenComparing(berdasarkanNomor);
            case STATUS:
                return Comparator.<Kamar, Boolean>comparing(Kamar::isTerisi)
                        .thenComparing(berdasarkanNomor);
            case JATUH_TEMPO:
                return Comparator.<Kamar, Boolean>comparing(k -> !k.isTerisi())
                        .thenComparing(Kamar::getTanggalJatuhTempo,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(berdasarkanNomor);
            case NOMOR_KAMAR:
            default:
                return berdasarkanNomor;
        }
    }

    static int bandingkanNomor(String a, String b) {
        int i = 0;
        int j = 0;
        while (i < a.length() && j < b.length()) {
            char ca = a.charAt(i);
            char cb = b.charAt(j);
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int awalA = i;
                int awalB = j;
                while (i < a.length() && Character.isDigit(a.charAt(i))) {
                    i++;
                }
                while (j < b.length() && Character.isDigit(b.charAt(j))) {
                    j++;
                }
                String angkaA = a.substring(awalA, i).replaceFirst("^0+(?=.)", "");
                String angkaB = b.substring(awalB, j).replaceFirst("^0+(?=.)", "");
                if (angkaA.length() != angkaB.length()) {
                    return angkaA.length() - angkaB.length();
                }
                int hasil = angkaA.compareTo(angkaB);
                if (hasil != 0) {
                    return hasil;
                }
            } else {
                int hasil = Character.compare(Character.toLowerCase(ca), Character.toLowerCase(cb));
                if (hasil != 0) {
                    return hasil;
                }
                i++;
                j++;
            }
        }
        return (a.length() - i) - (b.length() - j);
    }

    public Kamar cariKamar(String nomorKamar) {
        if (nomorKamar == null) {
            return null;
        }
        String nomor = nomorKamar.trim();
        for (Kamar k : daftarKamar) {
            if (k.getNomorKamar().equalsIgnoreCase(nomor)) {
                return k;
            }
        }
        return null;
    }

    private Kamar ambilKamar(String nomorKamar) {
        Kamar kamar = cariKamar(nomorKamar);
        if (kamar == null) {
            throw new IllegalArgumentException("Kamar " + nomorKamar + " tidak ditemukan.");
        }
        return kamar;
    }

    public List<Kamar> getKamarKosong() {
        List<Kamar> hasil = new ArrayList<>();
        for (Kamar k : daftarKamar) {
            if (!k.isTerisi()) {
                hasil.add(k);
            }
        }
        hasil.sort((a, b) -> bandingkanNomor(a.getNomorKamar(), b.getNomorKamar()));
        return hasil;
    }

    public int getJumlahKamar() {
        return daftarKamar.size();
    }

    public int getJumlahTerisi() {
        int jumlah = 0;
        for (Kamar k : daftarKamar) {
            if (k.isTerisi()) {
                jumlah++;
            }
        }
        return jumlah;
    }

    public int getJumlahKosong() {
        return getJumlahKamar() - getJumlahTerisi();
    }

    public int getJumlahBelumLunas() {
        int jumlah = 0;
        for (Kamar k : daftarKamar) {
            if (k.isBelumLunas()) {
                jumlah++;
            }
        }
        return jumlah;
    }

    public int getJumlahLewatJatuhTempo(LocalDate hariIni) {
        int jumlah = 0;
        for (Kamar k : daftarKamar) {
            if (k.getStatusJatuhTempo(hariIni) == StatusJatuhTempo.LEWAT) {
                jumlah++;
            }
        }
        return jumlah;
    }
}
