package com.kos.ui;

import com.kos.manager.KosManager;
import com.kos.model.Kamar;
import com.kos.model.Penyewa;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

public class DialogTambahPenyewa extends JDialog {

    private final KosManager manager;
    private final JComboBox<Kamar> comboKamar;
    private final JTextField fieldNik = new JTextField(16);
    private final JTextField fieldNama = new JTextField(16);
    private final JTextField fieldKontak = new JTextField(16);
    private final JSpinner spinnerMasuk = UiUtil.spinnerTanggal(LocalDate.now());
    private final JSpinner spinnerDurasi = new JSpinner(new SpinnerNumberModel(1, 1, 120, 1));
    private final JTextField fieldBayar = new JTextField(16);
    private final JLabel labelStatusBayar = UiUtil.label(" ", UiUtil.FONT_TEBAL, UiUtil.ABU);
    private final JLabel labelBiaya = UiUtil.label(" ", UiUtil.FONT_TEBAL, UiUtil.AKSEN);
    private final JLabel labelRincian = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.TEKS_PUDAR);
    private final JLabel labelError = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.BAHAYA);

    private Penyewa hasil;

    public DialogTambahPenyewa(Frame induk, KosManager manager, Kamar kamarAwal) {
        super(induk, "Tambah Penyewa ke Kamar", true);
        this.manager = manager;

        List<Kamar> kosong = manager.getKamarKosong();
        comboKamar = new JComboBox<>(kosong.toArray(new Kamar[0]));
        comboKamar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Kamar) {
                    Kamar k = (Kamar) value;
                    setText("Kamar " + k.getNomorKamar() + " - " + k.getJenisKamar() + " ("
                            + UiUtil.rupiah(k.getTarifPerBulan()) + "/bln)");
                }
                return this;
            }
        });
        if (kamarAwal != null && !kamarAwal.isTerisi()) {
            comboKamar.setSelectedItem(kamarAwal);
        }

        JPanel isi = new JPanel(new BorderLayout(0, 12));
        isi.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        isi.add(UiUtil.label("Tambah Penyewa ke Kamar", UiUtil.FONT_JUDUL, UiUtil.TEKS),
                BorderLayout.NORTH);
        isi.add(bangunForm(), BorderLayout.CENTER);
        isi.add(bangunBawah(), BorderLayout.SOUTH);
        setContentPane(isi);

        comboKamar.addActionListener(e -> isiBayarOtomatis());
        spinnerDurasi.addChangeListener(e -> isiBayarOtomatis());
        spinnerMasuk.addChangeListener(e -> perbaruiBiaya());
        fieldBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                perbaruiBiaya();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                perbaruiBiaya();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                perbaruiBiaya();
            }
        });
        isiBayarOtomatis();

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setMinimumSize(new Dimension(460, getHeight()));
        setResizable(false);
        setLocationRelativeTo(induk);
    }

    public Penyewa tampilkan() {
        setVisible(true);
        return hasil;
    }

    private JPanel bangunForm() {
        JPanel form = new JPanel(new GridBagLayout());
        DialogTambahKamar.tambahBaris(form, 0, "Kamar", comboKamar);
        DialogTambahKamar.tambahBaris(form, 1, "ID / NIK", fieldNik);
        DialogTambahKamar.tambahBaris(form, 2, "Nama penyewa", fieldNama);
        DialogTambahKamar.tambahBaris(form, 3, "Kontak (telepon)", fieldKontak);
        DialogTambahKamar.tambahBaris(form, 4, "Tanggal masuk", spinnerMasuk);
        DialogTambahKamar.tambahBaris(form, 5, "Durasi sewa (bulan)", spinnerDurasi);
        DialogTambahKamar.tambahBaris(form, 6, "Dibayar sekarang (Rp)", fieldBayar);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 0, 0, 0);
        c.gridy = 7;
        form.add(labelBiaya, c);
        c.gridy = 8;
        c.insets = new Insets(2, 0, 0, 0);
        form.add(labelRincian, c);
        c.gridy = 9;
        c.insets = new Insets(6, 0, 0, 0);
        form.add(labelStatusBayar, c);
        return form;
    }

    private JPanel bangunBawah() {
        UiUtil.Tombol simpan = new UiUtil.Tombol("Tempatkan Penyewa", UiUtil.AKSEN, Color.WHITE);
        UiUtil.Tombol batal = new UiUtil.Tombol("Batal", UiUtil.ABU_MUDA, UiUtil.TEKS);
        simpan.addActionListener(e -> simpan());
        batal.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(simpan);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.add(batal);
        tombol.add(simpan);

        JPanel bawah = new JPanel(new BorderLayout(0, 8));
        bawah.add(labelError, BorderLayout.NORTH);
        bawah.add(tombol, BorderLayout.CENTER);
        return bawah;
    }

    private void isiBayarOtomatis() {
        Kamar kamar = (Kamar) comboKamar.getSelectedItem();
        if (kamar == null) {
            return;
        }
        int durasi = (Integer) spinnerDurasi.getValue();
        fieldBayar.setText(String.valueOf(Math.round(kamar.hitungHargaTotal(durasi))));
        perbaruiBiaya();
    }

    private double nominalDibayar() {
        return fieldBayar.getText().trim().isEmpty()
                ? 0 : UiUtil.parseRupiah(fieldBayar.getText(), "Nominal dibayar");
    }

    private void perbaruiBiaya() {
        Kamar kamar = (Kamar) comboKamar.getSelectedItem();
        if (kamar == null) {
            return;
        }
        int durasi = (Integer) spinnerDurasi.getValue();
        LocalDate masuk = UiUtil.ambilTanggal(spinnerMasuk);
        double total = kamar.hitungHargaTotal(durasi);
        labelBiaya.setText("Total biaya sewa: " + UiUtil.rupiah(total));

        double dibayar;
        try {
            dibayar = nominalDibayar();
        } catch (NumberFormatException e) {
            labelRincian.setText(durasi + " bulan x " + UiUtil.rupiah(kamar.getTarifPerBulan()));
            labelStatusBayar.setForeground(UiUtil.BAHAYA);
            labelStatusBayar.setText("Nominal dibayar harus berupa angka.");
            return;
        }
        String jatuhTempo;
        if (dibayar >= total) {
            jatuhTempo = UiUtil.tanggal(masuk.plusMonths(durasi));
            labelStatusBayar.setForeground(UiUtil.HIJAU);
            labelStatusBayar.setText("Lunas");
        } else if (dibayar > 0) {
            jatuhTempo = UiUtil.tanggal(masuk) + " (bergeser setelah lunas)";
            labelStatusBayar.setForeground(UiUtil.ORANYE);
            labelStatusBayar.setText("Belum Lunas - sisa " + UiUtil.rupiah(total - dibayar));
        } else {
            jatuhTempo = UiUtil.tanggal(masuk) + " (belum ada pembayaran)";
            labelStatusBayar.setForeground(UiUtil.ABU);
            labelStatusBayar.setText("Belum bayar - sisa " + UiUtil.rupiah(total));
        }
        labelRincian.setText(durasi + " bulan x " + UiUtil.rupiah(kamar.getTarifPerBulan())
                + "   |   Jatuh tempo: " + jatuhTempo);
    }

    private void simpan() {
        Kamar kamar = (Kamar) comboKamar.getSelectedItem();
        if (kamar == null) {
            labelError.setText("Tidak ada kamar kosong yang bisa dipilih.");
            return;
        }
        try {
            hasil = manager.tambahPenyewa(kamar.getNomorKamar(), fieldNik.getText(),
                    fieldNama.getText(), fieldKontak.getText(), UiUtil.ambilTanggal(spinnerMasuk),
                    (Integer) spinnerDurasi.getValue(), nominalDibayar());
            dispose();
        } catch (IllegalArgumentException | IllegalStateException e) {
            labelError.setText(e.getMessage());
        }
    }
}
