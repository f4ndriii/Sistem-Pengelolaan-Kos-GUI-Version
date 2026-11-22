package com.kos.manager;

import com.kos.model.ItemSampah;
import com.kos.model.Kamar;
import com.kos.model.KamarPremium;
import com.kos.model.KamarStandard;
import com.kos.model.Pembayaran;
import com.kos.model.Penyewa;
import com.kos.model.Tagihan;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PenyimpananJson {

    public static final Path BERKAS_DEFAULT = Paths.get("data", "kos.json");

    private static final int VERSI = 1;
    private static final String DAFTAR = "daftar";
    private static final String SAMPAH = "sampah";
    private static final String ARSIP = "arsip";

    private PenyimpananJson() {
    }

    public static synchronized void simpan(KosManager manager, Path berkas) throws IOException {
        String isi = Json.tulis(keStruktur(manager));
        Path induk = berkas.toAbsolutePath().getParent();
        if (induk != null) {
            Files.createDirectories(induk);
        }
        Path sementara = berkas.resolveSibling(berkas.getFileName() + ".tmp");
        Files.writeString(sementara, isi, StandardCharsets.UTF_8);
        try {
            Files.move(sementara, berkas, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(sementara, berkas, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Map<String, Object> keStruktur(KosManager manager) {
        List<Kamar> kamarDaftar = manager.getSemuaKamar();
        List<ItemSampah> sampah = manager.getSampah();
        List<Penyewa> penyewa = manager.getSemuaPenyewa();
        List<Pembayaran> pembayaran = manager.getSemuaPembayaran();

        Map<Kamar, String> lokasi = new IdentityHashMap<>();
        Map<Kamar, LocalDateTime> waktuHapus = new IdentityHashMap<>();
        Set<Kamar> urut = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        List<Kamar> semua = new ArrayList<>();
        for (Kamar k : kamarDaftar) {
            tambah(k, DAFTAR, lokasi, urut, semua);
        }
        for (ItemSampah item : sampah) {
            tambah(item.getKamar(), SAMPAH, lokasi, urut, semua);
            waktuHapus.put(item.getKamar(), item.getWaktuDihapus());
        }
        for (Penyewa p : penyewa) {
            tambah(p.getKamar(), ARSIP, lokasi, urut, semua);
        }
        for (Pembayaran p : pembayaran) {
            tambah(p.getKamar(), ARSIP, lokasi, urut, semua);
        }

        Map<Kamar, Integer> idKamar = new IdentityHashMap<>();
        for (int i = 0; i < semua.size(); i++) {
            idKamar.put(semua.get(i), i + 1);
        }
        Map<Penyewa, Integer> idPenyewa = new IdentityHashMap<>();
        for (int i = 0; i < penyewa.size(); i++) {
            idPenyewa.put(penyewa.get(i), i + 1);
        }

        List<Object> jsonKamar = new ArrayList<>();
        for (Kamar k : semua) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", idKamar.get(k));
            m.put("jenis", k.getJenisKamar());
            m.put("nomor", k.getNomorKamar());
            m.put("hargaDasar", k.getHargaDasar());
            if (k instanceof KamarPremium kp) {
                m.put("biayaFasilitas", kp.getBiayaFasilitas());
            }
            m.put("lokasi", lokasi.get(k));
            if (waktuHapus.containsKey(k)) {
                m.put("waktuDihapus", waktuHapus.get(k).toString());
            }
            m.put("terisi", k.isTerisi());
            m.put("penyewaId", k.getPenyewaAktif() == null ? null : idPenyewa.get(k.getPenyewaAktif()));
            m.put("jatuhTempo", str(k.getTanggalJatuhTempo()));
            Tagihan t = k.getTagihanAktif();
            if (t == null) {
                m.put("tagihan", null);
            } else {
                Map<String, Object> mt = new LinkedHashMap<>();
                mt.put("jumlahBulan", t.getJumlahBulan());
                mt.put("total", t.getTotal());
                mt.put("terbayar", t.getTerbayar());
                m.put("tagihan", mt);
            }
            jsonKamar.add(m);
        }

        List<Object> jsonPenyewa = new ArrayList<>();
        for (Penyewa p : penyewa) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", idPenyewa.get(p));
            m.put("nik", p.getNik());
            m.put("nama", p.getNama());
            m.put("kontak", p.getKontak());
            m.put("kamarId", idKamar.get(p.getKamar()));
            m.put("tanggalMasuk", str(p.getTanggalMasuk()));
            m.put("tanggalKeluar", str(p.getTanggalKeluar()));
            jsonPenyewa.add(m);
        }

        List<Object> jsonBayar = new ArrayList<>();
        for (Pembayaran p : pembayaran) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("penyewaId", idPenyewa.get(p.getPenyewa()));
            m.put("kamarId", idKamar.get(p.getKamar()));
            m.put("jumlahBulan", p.getJumlahBulan());
            m.put("nominal", p.getNominal());
            m.put("tanggalBayar", str(p.getTanggalBayar()));
            m.put("jatuhTempoBerikutnya", str(p.getJatuhTempoBerikutnya()));
            m.put("sisaSetelahBayar", p.getSisaSetelahBayar());
            jsonBayar.add(m);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("versi", VERSI);
        root.put("kamar", jsonKamar);
        root.put("penyewa", jsonPenyewa);
        root.put("pembayaran", jsonBayar);
        return root;
    }

    private static void tambah(Kamar k, String tempat, Map<Kamar, String> lokasi,
            Set<Kamar> sudah, List<Kamar> semua) {
        if (sudah.add(k)) {
            lokasi.put(k, tempat);
            semua.add(k);
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    public static synchronized boolean muat(KosManager manager, Path berkas) throws IOException {
        if (!Files.exists(berkas)) {
            return false;
        }
        String teks = Files.readString(berkas, StandardCharsets.UTF_8);
        try {
            dariStruktur(manager, Json.baca(teks));
        } catch (IOException e) {
            throw new IOException("Isi berkas tidak valid: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new IOException("Isi berkas tidak valid: " + e, e);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static void dariStruktur(KosManager manager, Object akar) throws IOException {
        Map<String, Object> root = (Map<String, Object>) akar;
        Object versi = root.get("versi");
        if (versi instanceof Number n && n.intValue() > VERSI) {
            throw new IOException("Berkas dibuat oleh versi aplikasi yang lebih baru (versi " + n.intValue() + ")");
        }

        Map<Integer, Kamar> kamarById = new java.util.HashMap<>();
        List<Kamar> daftar = new ArrayList<>();
        List<ItemSampah> sampah = new ArrayList<>();
        Map<Integer, Integer> penyewaDiKamar = new java.util.HashMap<>(); // kamarId -> penyewaId
        Map<Integer, Map<String, Object>> dataKamar = new java.util.HashMap<>();

        for (Object o : daftarDari(root, "kamar")) {
            Map<String, Object> m = (Map<String, Object>) o;
            int id = intDari(m, "id");
            String jenis = strWajib(m, "jenis");
            String nomor = strWajib(m, "nomor");
            double harga = angkaWajib(m, "hargaDasar");
            Kamar k;
            if ("Premium".equals(jenis)) {
                k = new KamarPremium(nomor, harga, angkaWajib(m, "biayaFasilitas"));
            } else if ("Standard".equals(jenis)) {
                k = new KamarStandard(nomor, harga);
            } else {
                throw new IOException("Jenis kamar tidak dikenal: " + jenis);
            }
            if (kamarById.put(id, k) != null) {
                throw new IOException("ID kamar ganda: " + id);
            }
            dataKamar.put(id, m);
            String lokasi = String.valueOf(m.get("lokasi"));
            if (DAFTAR.equals(lokasi)) {
                daftar.add(k);
            } else if (SAMPAH.equals(lokasi)) {
                sampah.add(new ItemSampah(k, LocalDateTime.parse(strWajib(m, "waktuDihapus"))));
            }
        }

        List<Penyewa> semuaPenyewa = new ArrayList<>();
        Map<Integer, Penyewa> penyewaById = new java.util.HashMap<>();
        for (Object o : daftarDari(root, "penyewa")) {
            Map<String, Object> m = (Map<String, Object>) o;
            Kamar kamar = kamarById.get(intDari(m, "kamarId"));
            if (kamar == null) {
                throw new IOException("Penyewa merujuk kamar yang tidak ada");
            }
            Penyewa p = new Penyewa(strWajib(m, "nik"), strWajib(m, "nama"), strWajib(m, "kontak"),
                    kamar, LocalDate.parse(strWajib(m, "tanggalMasuk")));
            if (m.get("tanggalKeluar") != null) {
                p.keluar(LocalDate.parse((String) m.get("tanggalKeluar")));
            }
            penyewaById.put(intDari(m, "id"), p);
            semuaPenyewa.add(p);
        }

        for (Map.Entry<Integer, Map<String, Object>> e : dataKamar.entrySet()) {
            Map<String, Object> m = e.getValue();
            Kamar k = kamarById.get(e.getKey());
            if (Boolean.TRUE.equals(m.get("terisi"))) {
                Penyewa p = penyewaById.get(intDari(m, "penyewaId"));
                if (p == null) {
                    throw new IOException("Kamar " + k.getNomorKamar() + " terisi oleh penyewa yang tidak ada");
                }
                k.setTerisi(true);
                k.setPenyewaAktif(p);
                k.setTanggalJatuhTempo(LocalDate.parse(strWajib(m, "jatuhTempo")));
                Object t = m.get("tagihan");
                if (t != null) {
                    Map<String, Object> mt = (Map<String, Object>) t;
                    Tagihan tagihan = new Tagihan(intDari(mt, "jumlahBulan"), angkaWajib(mt, "total"));
                    tagihan.tambahBayar(angkaWajib(mt, "terbayar"));
                    k.setTagihanAktif(tagihan);
                }
            }
        }

        List<Pembayaran> semuaBayar = new ArrayList<>();
        for (Object o : daftarDari(root, "pembayaran")) {
            Map<String, Object> m = (Map<String, Object>) o;
            Penyewa p = penyewaById.get(intDari(m, "penyewaId"));
            Kamar k = kamarById.get(intDari(m, "kamarId"));
            if (p == null || k == null) {
                throw new IOException("Pembayaran merujuk penyewa/kamar yang tidak ada");
            }
            semuaBayar.add(new Pembayaran(p, k, intDari(m, "jumlahBulan"), angkaWajib(m, "nominal"),
                    LocalDate.parse(strWajib(m, "tanggalBayar")),
                    LocalDate.parse(strWajib(m, "jatuhTempoBerikutnya")),
                    angkaWajib(m, "sisaSetelahBayar")));
        }

        manager.muatData(daftar, semuaPenyewa, semuaBayar, sampah);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> daftarDari(Map<String, Object> m, String kunci) throws IOException {
        Object v = m.get(kunci);
        if (v == null) {
            return new ArrayList<>();
        }
        if (!(v instanceof List)) {
            throw new IOException("Properti '" + kunci + "' harus berupa array");
        }
        return (List<Object>) v;
    }

    private static String strWajib(Map<String, Object> m, String kunci) throws IOException {
        if (!(m.get(kunci) instanceof String s)) {
            throw new IOException("Properti '" + kunci + "' wajib berupa teks");
        }
        return s;
    }

    private static double angkaWajib(Map<String, Object> m, String kunci) throws IOException {
        if (!(m.get(kunci) instanceof Number n)) {
            throw new IOException("Properti '" + kunci + "' wajib berupa angka");
        }
        return n.doubleValue();
    }

    private static int intDari(Map<String, Object> m, String kunci) throws IOException {
        return (int) angkaWajib(m, kunci);
    }
}
