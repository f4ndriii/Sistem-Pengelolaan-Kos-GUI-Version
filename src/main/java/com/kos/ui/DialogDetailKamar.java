package com.kos.ui;

import com.kos.manager.KosManager;
import com.kos.model.Kamar;
import com.kos.model.KamarPremium;
import com.kos.model.Pembayaran;
import com.kos.model.Penyewa;
import com.kos.model.StatusJatuhTempo;
import com.kos.model.Tagihan;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class DialogDetailKamar extends JDialog {

    private final KosManager manager;
    private final Kamar kamar;
    private final Runnable setelahBerubah;

    private String pesanSukses = "";
    private JScrollPane scroll;

    private JSpinner spinnerHitung;
    private JLabel labelHasilHitung;
    private JSpinner spinnerBulanBayar;
    private JTextField fieldNominal;
    private JSpinner spinnerTanggalBayar;
    private JLabel labelInfoBayar;
    private JLabel labelPesanBayar;
    private UiUtil.Tombol tombolBayar;

    public DialogDetailKamar(Frame induk, KosManager manager, Kamar kamar, Runnable setelahBerubah) {
        super(induk, "Detail Kamar " + kamar.getNomorKamar(), true);
        this.manager = manager;
        this.kamar = kamar;
        this.setelahBerubah = setelahBerubah;

        susun();
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setMinimumSize(new Dimension(560, 360));
        aturUkuran();
        setLocationRelativeTo(induk);
    }

    private void susun() {
        int posisiScroll = scroll == null ? 0 : scroll.getVerticalScrollBar().getValue();

        JPanel isi = new JPanel();
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setBorder(BorderFactory.createEmptyBorder(16, 18, 10, 18));

        isi.add(bangunHeader());
        isi.add(jarak(10));
        isi.add(bangunInfo());
        isi.add(jarak(10));
        isi.add(bangunKalkulator());
        isi.add(jarak(10));
        if (kamar.isTerisi()) {
            isi.add(kamar.isBelumLunas() ? bangunFormPelunasan() : bangunFormBayar());
            isi.add(jarak(10));
            isi.add(bangunRiwayat());
        } else {
            JLabel catatan = UiUtil.label("<html>Kamar ini kosong. Gunakan tombol <b>Tambah Penyewa "
                    + "ke Kamar</b> di menu kiri untuk menempatkan penyewa.</html>",
                    UiUtil.FONT_KECIL, UiUtil.TEKS_PUDAR);
            catatan.setAlignmentX(LEFT_ALIGNMENT);
            isi.add(catatan);
        }

        scroll = new JScrollPane(isi);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        UiUtil.Tombol tutup = new UiUtil.Tombol("Tutup", UiUtil.ABU_MUDA, UiUtil.TEKS);
        tutup.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 8));
        bawah.add(tutup);

        Container konten = getContentPane();
        konten.removeAll();
        konten.setLayout(new BorderLayout());
        konten.add(scroll, BorderLayout.CENTER);
        konten.add(bawah, BorderLayout.SOUTH);

        spinnerHitung.addChangeListener(e -> perbaruiKalkulator());
        perbaruiKalkulator();
        if (kamar.isTerisi()) {
            perbaruiInfoBayar();
        }
        konten.revalidate();
        konten.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(posisiScroll));
    }

    private void aturUkuran() {
        pack();
        int tinggiMaks = GraphicsEnvironment.isHeadless() ? 820
                : GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - 40;
        setSize(Math.max(getWidth(), 640), Math.min(getHeight() + 6, tinggiMaks));
    }

    private static JPanel jarak(int tinggi) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(1, tinggi));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, tinggi));
        return p;
    }

    private JPanel bangunHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.add(UiUtil.label("Kamar " + kamar.getNomorKamar(), UiUtil.FONT_JUDUL, UiUtil.TEKS));
        boolean premium = kamar instanceof KamarPremium;
        header.add(new UiUtil.Lencana(kamar.getJenisKamar(), premium ? UiUtil.UNGU : UiUtil.ABU,
                premium ? UiUtil.UNGU_MUDA : UiUtil.ABU_MUDA));
        header.add(kamar.isTerisi()
                ? new UiUtil.Lencana("Terisi", UiUtil.BIRU, UiUtil.BIRU_MUDA)
                : new UiUtil.Lencana("Kosong", UiUtil.HIJAU, UiUtil.HIJAU_MUDA));
        if (kamar.isBelumLunas()) {
            header.add(new UiUtil.Lencana("Belum Lunas", UiUtil.ORANYE, UiUtil.ORANYE_MUDA));
        }
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
        return header;
    }

    private JPanel bangunInfo() {
        JPanel panel = kotak("Informasi Kamar & Penyewa");
        int baris = 0;
        if (kamar instanceof KamarPremium) {
            KamarPremium premium = (KamarPremium) kamar;
            tambahInfo(panel, baris++, "Tarif dasar", UiUtil.rupiah(kamar.getHargaDasar()) + " / bulan");
            tambahInfo(panel, baris++, "Biaya fasilitas",
                    UiUtil.rupiah(premium.getBiayaFasilitas()) + " / bulan");
            tambahInfo(panel, baris++, "Tarif per bulan",
                    UiUtil.rupiah(kamar.getTarifPerBulan()) + " / bulan");
        } else {
            tambahInfo(panel, baris++, "Tarif per bulan",
                    UiUtil.rupiah(kamar.getTarifPerBulan()) + " / bulan");
        }
        if (kamar.isTerisi()) {
            Penyewa p = kamar.getPenyewaAktif();
            tambahInfo(panel, baris++, "Nama penyewa", p.getNama());
            tambahInfo(panel, baris++, "ID / NIK", p.getNik());
            tambahInfo(panel, baris++, "Kontak", p.getKontak());
            tambahInfo(panel, baris++, "Tanggal masuk", UiUtil.tanggal(p.getTanggalMasuk()));

            LocalDate hariIni = LocalDate.now();
            StatusJatuhTempo status = kamar.getStatusJatuhTempo(hariIni);
            JPanel tempo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            tempo.setOpaque(false);
            tempo.add(UiUtil.label(UiUtil.ringkasanJatuhTempo(kamar, hariIni),
                    UiUtil.FONT_TEBAL, UiUtil.TEKS));
            tempo.add(Box.createHorizontalStrut(8));
            tempo.add(new UiUtil.Lencana(status.getLabel(), UiUtil.warnaTeks(status),
                    UiUtil.warnaLatar(status)));
            tambahBaris(panel, baris++, "Jatuh tempo", tempo);

            JPanel lunas = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            lunas.setOpaque(false);
            if (kamar.isBelumLunas()) {
                Tagihan t = kamar.getTagihanAktif();
                lunas.add(new UiUtil.Lencana("Belum Lunas", UiUtil.ORANYE, UiUtil.ORANYE_MUDA));
                lunas.add(Box.createHorizontalStrut(8));
                lunas.add(UiUtil.label("Sisa " + UiUtil.rupiah(t.getSisa()) + " dari "
                        + UiUtil.rupiah(t.getTotal()) + " (" + t.getJumlahBulan() + " bulan)",
                        UiUtil.FONT_TEBAL, UiUtil.ORANYE));
            } else {
                lunas.add(new UiUtil.Lencana("Lunas", UiUtil.HIJAU, UiUtil.HIJAU_MUDA));
            }
            tambahBaris(panel, baris, "Status pembayaran", lunas);
        }
        return panel;
    }

    private JPanel bangunKalkulator() {
        spinnerHitung = new JSpinner(new SpinnerNumberModel(1, 1, 120, 1));
        labelHasilHitung = UiUtil.label(" ", UiUtil.FONT_TEBAL, UiUtil.AKSEN);
        JPanel panel = kotak("Kalkulator Biaya Sewa");
        JPanel baris = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        baris.setOpaque(false);
        baris.add(spinnerHitung);
        baris.add(Box.createHorizontalStrut(10));
        baris.add(labelHasilHitung);
        tambahBaris(panel, 0, "Durasi sewa (bulan)", baris);
        return panel;
    }

    private JPanel bangunFormBayar() {
        spinnerBulanBayar = new JSpinner(new SpinnerNumberModel(1, 1, 120, 1));
        fieldNominal = new JTextField(12);
        spinnerTanggalBayar = UiUtil.spinnerTanggal(LocalDate.now());
        labelInfoBayar = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.TEKS_PUDAR);
        labelPesanBayar = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.HIJAU);
        tombolBayar = new UiUtil.Tombol("Catat Pembayaran", UiUtil.AKSEN, Color.WHITE);

        JPanel panel = kotak("Catat Pembayaran");
        tambahBaris(panel, 0, "Jumlah bulan dibayar", spinnerBulanBayar);
        tambahBaris(panel, 1, "Nominal dibayar (Rp)", fieldNominal);
        tambahBaris(panel, 2, "Tanggal bayar", spinnerTanggalBayar);
        susunBagianBawahForm(panel, 3);

        spinnerBulanBayar.addChangeListener(e -> isiNominalOtomatis());
        isiNominalOtomatis();
        return panel;
    }

    private JPanel bangunFormPelunasan() {
        Tagihan t = kamar.getTagihanAktif();
        fieldNominal = new JTextField(12);
        spinnerTanggalBayar = UiUtil.spinnerTanggal(LocalDate.now());
        labelInfoBayar = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.TEKS_PUDAR);
        labelPesanBayar = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.HIJAU);
        tombolBayar = new UiUtil.Tombol("Lunasi Tagihan", UiUtil.AKSEN, Color.WHITE);

        JPanel panel = kotak("Pelunasan Tagihan");
        tambahInfo(panel, 0, "Tagihan", t.getJumlahBulan() + " bulan - " + UiUtil.rupiah(t.getTotal()));
        tambahInfo(panel, 1, "Sudah dibayar", UiUtil.rupiah(t.getTerbayar()));
        tambahBaris(panel, 2, "Sisa tagihan",
                UiUtil.label(UiUtil.rupiah(t.getSisa()), UiUtil.FONT_TEBAL, UiUtil.ORANYE));
        tambahBaris(panel, 3, "Nominal dibayar (Rp)", fieldNominal);
        tambahBaris(panel, 4, "Tanggal bayar", spinnerTanggalBayar);
        susunBagianBawahForm(panel, 5);

        fieldNominal.setText(String.valueOf(Math.round(t.getSisa())));
        perbaruiInfoBayar();
        return panel;
    }

    private void susunBagianBawahForm(JPanel panel, int barisAwal) {
        fieldNominal.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                perbaruiInfoBayar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                perbaruiInfoBayar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                perbaruiInfoBayar();
            }
        });
        tombolBayar.addActionListener(e -> bayar());

        tambahBarisPenuh(panel, barisAwal, labelInfoBayar);
        tambahBarisPenuh(panel, barisAwal + 1, labelPesanBayar);
        labelPesanBayar.setText(pesanSukses);
        pesanSukses = "";
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tombol.setOpaque(false);
        tombol.add(tombolBayar);
        tambahBarisPenuh(panel, barisAwal + 2, tombol);
    }

    private JPanel bangunRiwayat() {
        DefaultTableModel modelRiwayat = new DefaultTableModel(
                new Object[] {"Tanggal Bayar", "Bulan", "Nominal", "Keterangan", "Jatuh Tempo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        double total = 0;
        List<Pembayaran> riwayat = manager.getRiwayatPembayaran(kamar.getPenyewaAktif());
        for (Pembayaran p : riwayat) {
            modelRiwayat.addRow(new Object[] {UiUtil.tanggal(p.getTanggalBayar()),
                p.getJumlahBulan(), UiUtil.rupiah(p.getNominal()),
                p.isLunas() ? "Lunas" : "Sisa " + UiUtil.rupiah(p.getSisaSetelahBayar()),
                UiUtil.tanggal(p.getJatuhTempoBerikutnya())});
            total += p.getNominal();
        }

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Riwayat Pembayaran"));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JTable tabel = new JTable(modelRiwayat);
        tabel.setRowHeight(24);
        tabel.setFillsViewportHeight(true);
        tabel.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer kanan = new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        tabel.getColumnModel().getColumn(2).setCellRenderer(kanan);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(50);

        JScrollPane scrollTabel = new JScrollPane(tabel);
        scrollTabel.setPreferredSize(new Dimension(540, 120));
        panel.add(scrollTabel, BorderLayout.CENTER);
        panel.add(UiUtil.label(riwayat.isEmpty() ? "Belum ada pembayaran tercatat."
                : riwayat.size() + " pembayaran, total diterima " + UiUtil.rupiah(total),
                UiUtil.FONT_KECIL, UiUtil.TEKS_PUDAR), BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height + 20));
        return panel;
    }

    private JPanel kotak(String judul) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(judul));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    private void tambahInfo(JPanel panel, int baris, String teks, String nilai) {
        tambahBaris(panel, baris, teks, UiUtil.label(nilai, UiUtil.FONT_TEBAL, UiUtil.TEKS));
    }

    private void tambahBaris(JPanel panel, int baris, String teks, JComponent komponen) {
        GridBagConstraints kiri = new GridBagConstraints();
        kiri.gridx = 0;
        kiri.gridy = baris;
        kiri.anchor = GridBagConstraints.WEST;
        kiri.insets = new Insets(3, 8, 3, 14);
        panel.add(UiUtil.label(teks, UiUtil.FONT_NORMAL, UiUtil.TEKS_PUDAR), kiri);

        GridBagConstraints kanan = new GridBagConstraints();
        kanan.gridx = 1;
        kanan.gridy = baris;
        kanan.weightx = 1;
        kanan.anchor = GridBagConstraints.WEST;
        kanan.fill = komponen instanceof JTextField || komponen instanceof JSpinner
                ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
        kanan.insets = new Insets(3, 0, 3, 8);
        panel.add(komponen, kanan);
    }

    private void tambahBarisPenuh(JPanel panel, int baris, JComponent komponen) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = baris;
        c.gridwidth = 2;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 8, 3, 8);
        panel.add(komponen, c);
    }

    private void perbaruiKalkulator() {
        int bulan = (Integer) spinnerHitung.getValue();
        labelHasilHitung.setText("= " + UiUtil.rupiah(kamar.hitungHargaTotal(bulan))
                + "  (" + bulan + " x " + UiUtil.rupiah(kamar.getTarifPerBulan()) + ")");
    }

    private void isiNominalOtomatis() {
        int bulan = (Integer) spinnerBulanBayar.getValue();
        fieldNominal.setText(String.valueOf(Math.round(kamar.hitungHargaTotal(bulan))));
    }

    private void perbaruiInfoBayar() {
        double nominal;
        try {
            nominal = UiUtil.parseRupiah(fieldNominal.getText(), "Nominal");
        } catch (NumberFormatException e) {
            infoBayar("Isi nominal dengan angka.", UiUtil.TEKS_PUDAR);
            return;
        }
        boolean pelunasan = kamar.isBelumLunas();
        double tagihan;
        int bulan;
        if (pelunasan) {
            tagihan = kamar.getTagihanAktif().getSisa();
            bulan = kamar.getTagihanAktif().getJumlahBulan();
        } else {
            bulan = (Integer) spinnerBulanBayar.getValue();
            tagihan = kamar.hitungHargaTotal(bulan);
        }
        if (nominal > tagihan) {
            infoBayar("Nominal melebihi " + (pelunasan ? "sisa tagihan" : "total tagihan")
                    + " (" + UiUtil.rupiah(tagihan) + ").", UiUtil.BAHAYA);
            tombolBayar.setEnabled(false);
            return;
        }
        tombolBayar.setEnabled(true);
        if (nominal >= tagihan) {
            infoBayar("Lunas. Jatuh tempo berikutnya: "
                    + UiUtil.tanggal(kamar.getTanggalJatuhTempo().plusMonths(bulan)), UiUtil.HIJAU);
            tombolBayar.setText(pelunasan ? "Lunasi Tagihan" : "Catat Pembayaran");
        } else {
            infoBayar("Belum lunas, sisa " + UiUtil.rupiah(tagihan - nominal)
                    + ". Jatuh tempo baru bergeser setelah tagihan lunas.", UiUtil.ORANYE);
            tombolBayar.setText(pelunasan ? "Bayar Sebagian" : "Catat Cicilan");
        }
    }

    private void infoBayar(String teks, Color warna) {
        labelInfoBayar.setText(teks);
        labelInfoBayar.setForeground(warna);
    }

    private void bayar() {
        try {
            double nominal = UiUtil.parseRupiah(fieldNominal.getText(), "Nominal");
            LocalDate tanggal = UiUtil.ambilTanggal(spinnerTanggalBayar);
            Pembayaran p = kamar.isBelumLunas()
                    ? manager.lunasiTagihan(kamar.getNomorKamar(), nominal, tanggal)
                    : manager.catatPembayaran(kamar.getNomorKamar(),
                            (Integer) spinnerBulanBayar.getValue(), nominal, tanggal);
            pesanSukses = p.isLunas() ? "Pembayaran tercatat. Tagihan lunas."
                    : "Pembayaran tercatat. Belum lunas, sisa " + UiUtil.rupiah(p.getSisaSetelahBayar()) + ".";
            setelahBerubah.run();
            susun();
            aturUkuran();
        } catch (IllegalArgumentException | IllegalStateException e) {
            labelPesanBayar.setForeground(UiUtil.BAHAYA);
            labelPesanBayar.setText(e.getMessage());
        }
    }
}
