package com.kos.ui;

import com.kos.manager.KosManager;
import com.kos.model.ItemSampah;
import com.kos.model.Kamar;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class DialogSampah extends JDialog {

    private final KosManager manager;
    private final Runnable setelahBerubah;

    private final DefaultTableModel modelTabel = new DefaultTableModel(
            new Object[] {"Nomor", "Jenis", "Tarif / Bulan", "Dihapus Pada"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabel = new JTable(modelTabel);
    private final JLabel labelKosong = UiUtil.label("Sampah kosong.", UiUtil.FONT_NORMAL, UiUtil.TEKS_PUDAR);
    private final JLabel labelPesan = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.HIJAU);
    private final UiUtil.Tombol tombolPulihkan = new UiUtil.Tombol("Pulihkan", UiUtil.AKSEN, Color.WHITE);
    private final UiUtil.Tombol tombolPermanen = new UiUtil.Tombol("Hapus Permanen", UiUtil.BAHAYA, Color.WHITE);
    private final UiUtil.Tombol tombolKosongkan = new UiUtil.Tombol("Kosongkan Sampah", UiUtil.ABU_MUDA, UiUtil.TEKS);

    private List<ItemSampah> daftar;
    private Kamar terakhirDipulihkan;

    public DialogSampah(Frame induk, KosManager manager, Runnable setelahBerubah) {
        super(induk, "Sampah", true);
        this.manager = manager;
        this.setelahBerubah = setelahBerubah;

        JPanel isi = new JPanel(new BorderLayout(0, 10));
        isi.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));

        JPanel atas = new JPanel(new BorderLayout(0, 4));
        atas.add(UiUtil.label("Sampah", UiUtil.FONT_JUDUL, UiUtil.TEKS), BorderLayout.NORTH);
        atas.add(UiUtil.label("Kamar yang dihapus disimpan di sini. Pilih kamar lalu klik Pulihkan "
                + "untuk mengembalikannya.", UiUtil.FONT_KECIL, UiUtil.TEKS_PUDAR), BorderLayout.SOUTH);
        isi.add(atas, BorderLayout.NORTH);

        tabel.setRowHeight(26);
        tabel.setFillsViewportHeight(true);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer kanan = new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        tabel.getColumnModel().getColumn(2).setCellRenderer(kanan);
        tabel.getSelectionModel().addListSelectionListener(e -> perbaruiTombol());

        JPanel tengah = new JPanel(new BorderLayout(0, 6));
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setPreferredSize(new Dimension(560, 230));
        tengah.add(scroll, BorderLayout.CENTER);
        JPanel status = new JPanel(new BorderLayout());
        status.add(labelPesan, BorderLayout.WEST);
        status.add(labelKosong, BorderLayout.EAST);
        tengah.add(status, BorderLayout.SOUTH);
        isi.add(tengah, BorderLayout.CENTER);

        JPanel tombol = new JPanel(new BorderLayout());
        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        kiri.add(tombolPulihkan);
        kiri.add(tombolPermanen);
        JPanel kananPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        UiUtil.Tombol tutup = new UiUtil.Tombol("Tutup", UiUtil.ABU_MUDA, UiUtil.TEKS);
        kananPanel.add(tombolKosongkan);
        kananPanel.add(tutup);
        tombol.add(kiri, BorderLayout.WEST);
        tombol.add(kananPanel, BorderLayout.EAST);
        isi.add(tombol, BorderLayout.SOUTH);
        setContentPane(isi);

        tombolPulihkan.addActionListener(e -> pulihkan());
        tombolPermanen.addActionListener(e -> hapusPermanen());
        tombolKosongkan.addActionListener(e -> kosongkanSampah());
        tutup.addActionListener(e -> dispose());

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        muat();
        pack();
        setMinimumSize(new Dimension(640, 360));
        setLocationRelativeTo(induk);
    }

    public Kamar tampilkan() {
        setVisible(true);
        return terakhirDipulihkan;
    }

    private void muat() {
        daftar = manager.getSampah();
        modelTabel.setRowCount(0);
        for (ItemSampah item : daftar) {
            Kamar k = item.getKamar();
            modelTabel.addRow(new Object[] {k.getNomorKamar(), k.getJenisKamar(),
                UiUtil.rupiah(k.getTarifPerBulan()), UiUtil.waktu(item.getWaktuDihapus())});
        }
        labelKosong.setVisible(daftar.isEmpty());
        perbaruiTombol();
    }

    private void perbaruiTombol() {
        boolean ada = tabel.getSelectedRow() >= 0;
        tombolPulihkan.setEnabled(ada);
        tombolPermanen.setEnabled(ada);
        tombolKosongkan.setEnabled(!daftar.isEmpty());
    }

    private Kamar kamarTerpilih() {
        int baris = tabel.getSelectedRow();
        return baris < 0 ? null : daftar.get(baris).getKamar();
    }

    private void pulihkan() {
        Kamar kamar = kamarTerpilih();
        if (kamar == null) {
            return;
        }
        try {
            manager.pulihkanKamar(kamar);
            terakhirDipulihkan = kamar;
            labelPesan.setForeground(UiUtil.HIJAU);
            labelPesan.setText("Kamar " + kamar.getNomorKamar() + " dipulihkan.");
            setelahBerubah.run();
            muat();
        } catch (IllegalArgumentException | IllegalStateException e) {
            labelPesan.setForeground(UiUtil.BAHAYA);
            labelPesan.setText(e.getMessage());
        }
    }

    private void hapusPermanen() {
        Kamar kamar = kamarTerpilih();
        if (kamar == null || !UiUtil.konfirmasi(this, "Hapus Permanen", "Hapus permanen kamar "
                + kamar.getNomorKamar() + "? Kamar ini tidak bisa dipulihkan lagi.")) {
            return;
        }
        manager.hapusPermanen(kamar);
        labelPesan.setForeground(UiUtil.TEKS_PUDAR);
        labelPesan.setText("Kamar " + kamar.getNomorKamar() + " dihapus permanen.");
        setelahBerubah.run();
        muat();
    }

    private void kosongkanSampah() {
        if (!UiUtil.konfirmasi(this, "Kosongkan Sampah", "Hapus permanen semua kamar di Sampah "
                + "(" + daftar.size() + " kamar)? Tindakan ini tidak bisa dibatalkan.")) {
            return;
        }
        manager.kosongkanSampah();
        labelPesan.setForeground(UiUtil.TEKS_PUDAR);
        labelPesan.setText("Sampah dikosongkan.");
        setelahBerubah.run();
        muat();
    }
}
