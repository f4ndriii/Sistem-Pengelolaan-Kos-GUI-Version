package com.kos.ui;

import com.kos.manager.KosManager;
import com.kos.model.Kamar;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class DialogTambahKamar extends JDialog {
    private static final String STANDARD = "Standard";
    private static final String PREMIUM = "Premium";

    private final KosManager manager;
    private final JComboBox<String> comboTipe = new JComboBox<>(new String[] {STANDARD, PREMIUM});
    private final JTextField fieldNomor = new JTextField(14);
    private final JTextField fieldHarga = new JTextField(14);
    private final JTextField fieldFasilitas = new JTextField(14);
    private final JLabel labelTarif = UiUtil.label(" ", UiUtil.FONT_TEBAL, UiUtil.AKSEN);
    private final JLabel labelError = UiUtil.label(" ", UiUtil.FONT_KECIL, UiUtil.BAHAYA);

    private Kamar hasil;

    public DialogTambahKamar(Frame induk, KosManager manager) {
        super(induk, "Tambah Kamar", true);
        this.manager = manager;

        JPanel isi = new JPanel(new BorderLayout(0, 12));
        isi.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        isi.add(UiUtil.label("Tambah Kamar Baru", UiUtil.FONT_JUDUL, UiUtil.TEKS), BorderLayout.NORTH);
        isi.add(bangunForm(), BorderLayout.CENTER);
        isi.add(bangunBawah(), BorderLayout.SOUTH);
        setContentPane(isi);

        comboTipe.addActionListener(e -> aturTipe());
        DocumentListener pembaru = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                perbaruiTarif();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                perbaruiTarif();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                perbaruiTarif();
            }
        };
        fieldHarga.getDocument().addDocumentListener(pembaru);
        fieldFasilitas.getDocument().addDocumentListener(pembaru);
        aturTipe();

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setMinimumSize(new Dimension(400, getHeight()));
        setResizable(false);
        setLocationRelativeTo(induk);
    }

    public Kamar tampilkan() {
        setVisible(true);
        return hasil;
    }

    private JPanel bangunForm() {
        JPanel form = new JPanel(new GridBagLayout());
        tambahBaris(form, 0, "Tipe kamar", comboTipe);
        tambahBaris(form, 1, "Nomor kamar", fieldNomor);
        tambahBaris(form, 2, "Tarif dasar / bulan (Rp)", fieldHarga);
        tambahBaris(form, 3, "Biaya fasilitas / bulan (Rp)", fieldFasilitas);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 0, 0, 0);
        form.add(labelTarif, c);
        return form;
    }

    static void tambahBaris(JPanel panel, int baris, String teks, JComponent komponen) {
        GridBagConstraints kiri = new GridBagConstraints();
        kiri.gridx = 0;
        kiri.gridy = baris;
        kiri.anchor = GridBagConstraints.WEST;
        kiri.insets = new Insets(4, 0, 4, 14);
        panel.add(UiUtil.label(teks, UiUtil.FONT_NORMAL, UiUtil.TEKS), kiri);

        GridBagConstraints kanan = new GridBagConstraints();
        kanan.gridx = 1;
        kanan.gridy = baris;
        kanan.weightx = 1;
        kanan.fill = GridBagConstraints.HORIZONTAL;
        kanan.insets = new Insets(4, 0, 4, 0);
        panel.add(komponen, kanan);
    }

    private JPanel bangunBawah() {
        UiUtil.Tombol simpan = new UiUtil.Tombol("Simpan", UiUtil.AKSEN, java.awt.Color.WHITE);
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

    private boolean premium() {
        return PREMIUM.equals(comboTipe.getSelectedItem());
    }

    private void aturTipe() {
        fieldFasilitas.setEnabled(premium());
        if (!premium()) {
            fieldFasilitas.setText("");
        }
        perbaruiTarif();
    }

    private void perbaruiTarif() {
        try {
            double harga = UiUtil.parseRupiah(fieldHarga.getText(), "Tarif dasar");
            double fasilitas = 0;
            if (premium() && !fieldFasilitas.getText().trim().isEmpty()) {
                fasilitas = UiUtil.parseRupiah(fieldFasilitas.getText(), "Biaya fasilitas");
            }
            labelTarif.setText("Tarif per bulan: " + UiUtil.rupiah(harga + fasilitas));
        } catch (NumberFormatException e) {
            labelTarif.setText(" ");
        }
    }

    private void simpan() {
        try {
            String nomor = fieldNomor.getText();
            double harga = UiUtil.parseRupiah(fieldHarga.getText(), "Tarif dasar");
            if (premium()) {
                double fasilitas = fieldFasilitas.getText().trim().isEmpty()
                        ? 0 : UiUtil.parseRupiah(fieldFasilitas.getText(), "Biaya fasilitas");
                hasil = manager.tambahKamarPremium(nomor, harga, fasilitas);
            } else {
                hasil = manager.tambahKamarStandard(nomor, harga);
            }
            dispose();
        } catch (IllegalArgumentException e) { // termasuk NumberFormatException
            labelError.setText(e.getMessage());
        }
    }
}
