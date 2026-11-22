package com.kos.ui;

import com.kos.manager.KosManager;
import com.kos.manager.KosManager.UrutanKamar;
import com.kos.manager.PenyimpananJson;
import com.kos.model.Kamar;
import com.kos.model.Penyewa;
import com.kos.model.StatusJatuhTempo;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;

public class MainFrame extends JFrame implements KamarCardPanel.Listener {
    private static final String HALAMAN_GRID = "grid";
    private static final String HALAMAN_KOSONG = "kosong";
    private static final String PETUNJUK_CARI = "Cari berdasarkan nama penyewa atau nomor kamar...";

    private final KosManager manager;

    private final JTextField fieldCari = new JTextField() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                g.setColor(UiUtil.TEKS_PUDAR);
                g.setFont(UiUtil.FONT_NORMAL);
                int y = (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2;
                g.drawString(PETUNJUK_CARI, getInsets().left + 2, y);
            }
        }
    };
    private final JComboBox<UrutanKamar> comboUrut = new JComboBox<>(UrutanKamar.values());
    private final PanelGrid panelGrid = new PanelGrid();
    private final JScrollPane scrollGrid = new JScrollPane(panelGrid);
    private final CardLayout layoutTengah = new CardLayout();
    private final JPanel panelTengah = new JPanel(layoutTengah);
    private final JLabel labelKosong = new JLabel("", SwingConstants.CENTER);

    private final JLabel labelTotal = UiUtil.label(" ", UiUtil.FONT_NORMAL, Color.WHITE);
    private final JLabel labelTerisi = UiUtil.label(" ", UiUtil.FONT_NORMAL, Color.WHITE);
    private final JLabel labelKosongRingkas = UiUtil.label(" ", UiUtil.FONT_NORMAL, Color.WHITE);
    private final JLabel labelLewat = UiUtil.label(" ", UiUtil.FONT_NORMAL, Color.WHITE);
    private final JLabel labelBelumLunas = UiUtil.label(" ", UiUtil.FONT_NORMAL, Color.WHITE);
    private UiUtil.Tombol tombolSampah;
    private final JLabel labelTerpilih = UiUtil.label(" ", UiUtil.FONT_TEBAL, Color.WHITE);

    private final Map<String, KamarCardPanel> kartu = new HashMap<>();
    private String nomorTerpilih;

    public static void jalankan(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiUtil.aturLookAndFeel();
            KosManager manager = new KosManager();
            String pesanMuat = muatDariBerkas(manager);
            MainFrame frame = new MainFrame(manager);
            aktifkanSimpanOtomatis(manager, frame);
            frame.setVisible(true);
            if (pesanMuat != null) {
                UiUtil.pesanError(frame, pesanMuat);
            }
        });
    }

    private static String muatDariBerkas(KosManager manager) {
        Path berkas = PenyimpananJson.BERKAS_DEFAULT;
        try {
            PenyimpananJson.muat(manager, berkas);
            return null;
        } catch (IOException e) {
            String stempel = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path cadangan = berkas.resolveSibling(berkas.getFileName() + ".rusak-" + stempel);
            try {
                Files.move(berkas, cadangan, StandardCopyOption.REPLACE_EXISTING);
                return "Data tersimpan tidak bisa dibaca:\n" + e.getMessage()
                        + "\n\nBerkas lama disimpan sebagai:\n" + cadangan.toAbsolutePath()
                        + "\n\nAplikasi dimulai dengan data kosong.";
            } catch (IOException e2) {
                return "Data tersimpan tidak bisa dibaca:\n" + e.getMessage()
                        + "\n\nBerkas tidak dapat dicadangkan (" + e2.getMessage()
                        + ").\nAplikasi dimulai dengan data kosong.";
            }
        }
    }

    private static void aktifkanSimpanOtomatis(KosManager manager, Component induk) {
        boolean[] sudahDilaporkan = {false};
        manager.setPendengarPerubahan(() -> {
            try {
                PenyimpananJson.simpan(manager, PenyimpananJson.BERKAS_DEFAULT);
                sudahDilaporkan[0] = false;
            } catch (IOException | RuntimeException e) {
                if (!sudahDilaporkan[0]) {
                    sudahDilaporkan[0] = true;
                    UiUtil.pesanError(induk, "Gagal menyimpan data ke "
                            + PenyimpananJson.BERKAS_DEFAULT.toAbsolutePath() + ":\n" + e.getMessage());
                }
            }
        });
    }

    public MainFrame(KosManager manager) {
        super("Sistem Pengelolaan Kos");
        this.manager = manager;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                keluar();
            }
        });

        setLayout(new BorderLayout());
        add(bangunSidebar(), BorderLayout.WEST);

        JPanel konten = new JPanel(new BorderLayout());
        konten.setBackground(UiUtil.LATAR);
        konten.add(bangunTopbar(), BorderLayout.NORTH);
        konten.add(bangunAreaGrid(), BorderLayout.CENTER);
        add(konten, BorderLayout.CENTER);

        fieldCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });
        comboUrut.addActionListener(e -> refresh());

        setSize(1200, 740);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        refresh();
    }

    private JPanel bangunSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UiUtil.SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 18, 20, 18));
        sidebar.setPreferredSize(new Dimension(250, 100));

        JLabel judul = new JLabel("<html>Sistem<br>Pengelolaan Kos</html>");
        judul.setFont(UiUtil.FONT_JUDUL.deriveFont(22f));
        judul.setForeground(Color.WHITE);
        judul.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(judul);
        sidebar.add(Box.createVerticalStrut(4));
        JLabel sub = UiUtil.label("Panel Pemilik Kos", UiUtil.FONT_KECIL, new Color(0x9FB0D0));
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(sub);
        sidebar.add(Box.createVerticalStrut(28));

        sidebar.add(tombolSidebar("Tambah Kamar", UiUtil.AKSEN, e -> aksiTambahKamar()));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(tombolSidebar("Hapus Kamar", UiUtil.BAHAYA, e -> aksiHapusKamar()));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(tombolSidebar("Tambah Penyewa ke Kamar", UiUtil.AKSEN, e -> aksiTambahPenyewa()));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(tombolSidebar("Kosongkan Kamar", new Color(0xD97706), e -> aksiKosongkanKamar()));
        sidebar.add(Box.createVerticalStrut(10));
        tombolSampah = tombolSidebar("Sampah", new Color(0x475569), e -> aksiSampah());
        sidebar.add(tombolSampah);

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(label("RINGKASAN", UiUtil.FONT_KECIL, new Color(0x9FB0D0)));
        sidebar.add(Box.createVerticalStrut(6));
        for (JLabel l : new JLabel[] {labelTotal, labelTerisi, labelKosongRingkas, labelLewat, labelBelumLunas}) {
            l.setAlignmentX(LEFT_ALIGNMENT);
            sidebar.add(l);
            sidebar.add(Box.createVerticalStrut(3));
        }
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(label("KAMAR TERPILIH", UiUtil.FONT_KECIL, new Color(0x9FB0D0)));
        sidebar.add(Box.createVerticalStrut(6));
        labelTerpilih.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(labelTerpilih);
        return sidebar;
    }

    private static JLabel label(String teks, java.awt.Font font, Color warna) {
        JLabel l = UiUtil.label(teks, font, warna);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private UiUtil.Tombol tombolSidebar(String teks, Color warna, java.awt.event.ActionListener aksi) {
        UiUtil.Tombol tombol = new UiUtil.Tombol(teks, warna, Color.WHITE);
        tombol.addActionListener(aksi);
        tombol.setAlignmentX(LEFT_ALIGNMENT);
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, tombol.getPreferredSize().height + 6));
        return tombol;
    }

    private JPanel bangunTopbar() {
        JPanel topbar = new JPanel(new BorderLayout(16, 0));
        topbar.setBackground(Color.WHITE);
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiUtil.GARIS),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        fieldCari.setFont(UiUtil.FONT_NORMAL);
        fieldCari.setPreferredSize(new Dimension(300, 34));
        fieldCari.setToolTipText("Ketik nama penyewa atau nomor kamar; hasil tampil langsung");
        topbar.add(fieldCari, BorderLayout.CENTER);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kanan.setOpaque(false);
        kanan.add(UiUtil.label("Sort by", UiUtil.FONT_TEBAL, UiUtil.TEKS));
        comboUrut.setPreferredSize(new Dimension(230, 34));
        kanan.add(comboUrut);
        topbar.add(kanan, BorderLayout.EAST);
        return topbar;
    }

    private JPanel bangunAreaGrid() {
        scrollGrid.setBorder(null);
        scrollGrid.getViewport().setBackground(UiUtil.LATAR);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(24);

        labelKosong.setFont(UiUtil.FONT_NORMAL.deriveFont(15f));
        labelKosong.setForeground(UiUtil.TEKS_PUDAR);
        JPanel halamanKosong = new JPanel(new BorderLayout());
        halamanKosong.setBackground(UiUtil.LATAR);
        halamanKosong.add(labelKosong, BorderLayout.CENTER);

        panelTengah.add(scrollGrid, HALAMAN_GRID);
        panelTengah.add(halamanKosong, HALAMAN_KOSONG);
        return panelTengah;
    }

    private static class PanelGrid extends JPanel implements Scrollable {
        PanelGrid() {
            super(new GridLayout(0, 3, 16, 16));
            setBackground(UiUtil.LATAR);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 48, 24);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public void refresh() {
        LocalDate hariIni = LocalDate.now();
        int posisiScroll = scrollGrid.getVerticalScrollBar().getValue();

        if (nomorTerpilih != null && manager.cariKamar(nomorTerpilih) == null) {
            nomorTerpilih = null;
        }

        String kataKunci = fieldCari.getText().trim();
        List<Kamar> daftar = manager.getDaftarKamar(kataKunci, (UrutanKamar) comboUrut.getSelectedItem());

        panelGrid.removeAll();
        kartu.clear();
        for (Kamar kamar : daftar) {
            KamarCardPanel card = new KamarCardPanel(kamar, hariIni,
                    kamar.getNomorKamar().equals(nomorTerpilih), this);
            kartu.put(kamar.getNomorKamar(), card);
            panelGrid.add(card);
        }

        if (daftar.isEmpty()) {
            labelKosong.setText(manager.getJumlahKamar() == 0
                    ? "<html><div style='text-align:center'>Belum ada kamar.<br>"
                    + "Klik <b>Tambah Kamar</b> untuk memulai.</div></html>"
                    : "<html><div style='text-align:center'>Tidak ada kamar atau penyewa yang cocok dengan \""
                    + escapeHtml(kataKunci) + "\".</div></html>");
            layoutTengah.show(panelTengah, HALAMAN_KOSONG);
        } else {
            layoutTengah.show(panelTengah, HALAMAN_GRID);
        }
        panelGrid.revalidate();
        panelGrid.repaint();
        SwingUtilities.invokeLater(() -> scrollGrid.getVerticalScrollBar().setValue(posisiScroll));

        perbaruiRingkasan(hariIni);
    }

    private static String escapeHtml(String teks) {
        return teks.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void perbaruiRingkasan(LocalDate hariIni) {
        labelTotal.setText("Total kamar: " + manager.getJumlahKamar());
        labelTerisi.setText("Terisi: " + manager.getJumlahTerisi());
        labelKosongRingkas.setText("Kosong: " + manager.getJumlahKosong());
        int lewat = manager.getJumlahLewatJatuhTempo(hariIni);
        labelLewat.setText("Lewat jatuh tempo: " + lewat);
        labelLewat.setForeground(lewat > 0 ? new Color(0xFCA5A5) : Color.WHITE);
        int belumLunas = manager.getJumlahBelumLunas();
        labelBelumLunas.setText("Belum lunas: " + belumLunas);
        labelBelumLunas.setForeground(belumLunas > 0 ? new Color(0xFDBA74) : Color.WHITE);
        tombolSampah.setText(manager.getJumlahSampah() == 0
                ? "Sampah" : "Sampah (" + manager.getJumlahSampah() + ")");
        labelTerpilih.setText(nomorTerpilih == null ? "Belum ada (klik kartu kamar)"
                : "Kamar " + nomorTerpilih);
    }

    @Override
    public void kamarDipilih(Kamar kamar) {
        KamarCardPanel lama = nomorTerpilih == null ? null : kartu.get(nomorTerpilih);
        if (lama != null) {
            lama.setTerpilih(false);
        }
        nomorTerpilih = kamar.getNomorKamar();
        KamarCardPanel baru = kartu.get(nomorTerpilih);
        if (baru != null) {
            baru.setTerpilih(true);
        }
        labelTerpilih.setText("Kamar " + nomorTerpilih);
    }

    @Override
    public void kamarDibuka(Kamar kamar) {
        new DialogDetailKamar(this, manager, kamar, this::refresh).setVisible(true);
        refresh();
    }

    private Kamar kamarTerpilih() {
        return nomorTerpilih == null ? null : manager.cariKamar(nomorTerpilih);
    }

    private void pilihDanTampilkan(Kamar kamar) {
        refresh(); // kartu baru dibuat dulu supaya bisa dipilih
        kamarDipilih(kamar);
        KamarCardPanel card = kartu.get(kamar.getNomorKamar());
        if (card != null) {
            SwingUtilities.invokeLater(() -> card.scrollRectToVisible(card.getBounds()));
        }
    }

    private void aksiTambahKamar() {
        Kamar baru = new DialogTambahKamar(this, manager).tampilkan();
        if (baru != null) {
            fieldCari.setText(""); // pastikan kamar baru tidak tersaring
            pilihDanTampilkan(baru);
        }
    }

    private void aksiHapusKamar() {
        Kamar kamar = kamarTerpilih();
        if (kamar == null) {
            UiUtil.pesanInfo(this, "Pilih kamar terlebih dahulu dengan mengklik kartunya.");
            return;
        }
        if (kamar.isTerisi()) {
            UiUtil.pesanError(this, "Kamar " + kamar.getNomorKamar() + " masih terisi oleh "
                    + kamar.getPenyewaAktif().getNama() + ".\nKosongkan kamar terlebih dahulu "
                    + "sebelum menghapusnya.");
            return;
        }
        if (!UiUtil.konfirmasi(this, "Hapus Kamar",
                "Pindahkan kamar " + kamar.getNomorKamar() + " (" + kamar.getJenisKamar()
                + ") ke Sampah?\nKamar masih bisa dipulihkan lewat tombol Sampah.")) {
            return;
        }
        try {
            manager.hapusKamar(kamar.getNomorKamar());
            refresh();
        } catch (IllegalArgumentException | IllegalStateException e) {
            UiUtil.pesanError(this, e.getMessage());
        }
    }

    private void aksiSampah() {
        Kamar dipulihkan = new DialogSampah(this, manager, this::refresh).tampilkan();
        if (dipulihkan != null && manager.cariKamar(dipulihkan.getNomorKamar()) == dipulihkan) {
            fieldCari.setText("");
            pilihDanTampilkan(dipulihkan);
        }
    }

    private void aksiTambahPenyewa() {
        Kamar terpilih = kamarTerpilih();
        if (terpilih != null && terpilih.isTerisi()) {
            UiUtil.pesanError(this, "Kamar " + terpilih.getNomorKamar() + " sudah terisi oleh "
                    + terpilih.getPenyewaAktif().getNama() + ".\nKosongkan kamar terlebih dahulu, "
                    + "atau pilih kamar yang berstatus Kosong.");
            return;
        }
        if (manager.getKamarKosong().isEmpty()) {
            UiUtil.pesanInfo(this, manager.getJumlahKamar() == 0
                    ? "Belum ada kamar. Tambahkan kamar terlebih dahulu."
                    : "Semua kamar sedang terisi. Kosongkan atau tambah kamar terlebih dahulu.");
            return;
        }
        Penyewa baru = new DialogTambahPenyewa(this, manager, terpilih).tampilkan();
        if (baru != null) {
            fieldCari.setText("");
            pilihDanTampilkan(baru.getKamar());
        }
    }

    private void aksiKosongkanKamar() {
        Kamar kamar = kamarTerpilih();
        if (kamar == null) {
            UiUtil.pesanInfo(this, "Pilih kamar terlebih dahulu dengan mengklik kartunya.");
            return;
        }
        if (!kamar.isTerisi()) {
            UiUtil.pesanInfo(this, "Kamar " + kamar.getNomorKamar() + " sudah kosong.");
            return;
        }
        String pesan = "Kosongkan kamar " + kamar.getNomorKamar() + " dan akhiri sewa "
                + kamar.getPenyewaAktif().getNama() + "?";
        if (kamar.isBelumLunas()) {
            pesan += "\n\nPerhatian: penyewa ini masih punya tagihan belum lunas, sisa "
                    + UiUtil.rupiah(kamar.getTagihanAktif().getSisa()) + ".";
        }
        if (kamar.getStatusJatuhTempo(LocalDate.now()) == StatusJatuhTempo.LEWAT) {
            pesan += "\n\nPerhatian: penyewa ini sudah lewat jatuh tempo, yaitu "
                    + UiUtil.ringkasanJatuhTempo(kamar, LocalDate.now()) + ".";
        }
        if (!UiUtil.konfirmasi(this, "Kosongkan Kamar", pesan)) {
            return;
        }
        try {
            manager.kosongkanKamar(kamar.getNomorKamar());
            refresh();
        } catch (IllegalStateException | IllegalArgumentException e) {
            UiUtil.pesanError(this, e.getMessage());
        }
    }

    private void keluar() {
        if (manager.getJumlahKamar() > 0 && !UiUtil.konfirmasi(this, "Keluar",
                "Semua perubahan sudah tersimpan otomatis.\nTutup aplikasi?")) {
            return;
        }
        manager.simpanSekarang();
        dispose();
        System.exit(0);
    }
}
