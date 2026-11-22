package com.kos.ui;

import com.kos.model.Kamar;
import com.kos.model.KamarPremium;
import com.kos.model.StatusJatuhTempo;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import javax.swing.*;

public class KamarCardPanel extends JPanel {

    public interface Listener {
        void kamarDipilih(Kamar kamar);

        void kamarDibuka(Kamar kamar);
    }

    private static final int RADIUS = 16;

    private final Kamar kamar;
    private boolean terpilih;

    public KamarCardPanel(Kamar kamar, LocalDate hariIni, boolean terpilih, Listener listener) {
        this.kamar = kamar;
        this.terpilih = terpilih;

        setOpaque(false);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        setPreferredSize(new Dimension(250, 176));

        add(bangunBagianAtas(), BorderLayout.NORTH);
        add(bangunBagianTengah(hariIni), BorderLayout.CENTER);
        add(bangunBagianBawah(listener), BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                listener.kamarDipilih(kamar);
                if (e.getClickCount() == 2) {
                    listener.kamarDibuka(kamar);
                }
            }
        });
        setToolTipText("Klik untuk memilih, klik dua kali untuk membuka detail");
    }

    public Kamar getKamar() {
        return kamar;
    }

    public void setTerpilih(boolean terpilih) {
        if (this.terpilih != terpilih) {
            this.terpilih = terpilih;
            repaint();
        }
    }

    private JPanel bangunBagianAtas() {
        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(UiUtil.label("Kamar " + kamar.getNomorKamar(), UiUtil.FONT_JUDUL, UiUtil.TEKS),
                BorderLayout.WEST);

        boolean premium = kamar instanceof KamarPremium;
        UiUtil.Lencana jenis = new UiUtil.Lencana(kamar.getJenisKamar(),
                premium ? UiUtil.UNGU : UiUtil.ABU, premium ? UiUtil.UNGU_MUDA : UiUtil.ABU_MUDA);
        JPanel bungkus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bungkus.setOpaque(false);
        bungkus.add(jenis);
        atas.add(bungkus, BorderLayout.EAST);
        return atas;
    }

    private JPanel bangunBagianTengah(LocalDate hariIni) {
        JPanel tengah = new JPanel();
        tengah.setOpaque(false);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));

        UiUtil.Lencana status = kamar.isTerisi()
                ? new UiUtil.Lencana("Terisi", UiUtil.BIRU, UiUtil.BIRU_MUDA)
                : new UiUtil.Lencana("Kosong", UiUtil.HIJAU, UiUtil.HIJAU_MUDA);
        JPanel barisStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barisStatus.setOpaque(false);
        barisStatus.add(status);
        StatusJatuhTempo statusTempo = kamar.getStatusJatuhTempo(hariIni);
        if (kamar.isBelumLunas()) {
            // Sudah bayar sebagian: tampilkan "Belum Lunas", bukan "Aman".
            barisStatus.add(Box.createHorizontalStrut(6));
            barisStatus.add(new UiUtil.Lencana("Belum Lunas", UiUtil.ORANYE, UiUtil.ORANYE_MUDA));
        } else if (statusTempo != null) {
            barisStatus.add(Box.createHorizontalStrut(6));
            barisStatus.add(new UiUtil.Lencana(statusTempo.getLabel(),
                    UiUtil.warnaTeks(statusTempo), UiUtil.warnaLatar(statusTempo)));
        }
        barisStatus.setAlignmentX(LEFT_ALIGNMENT);
        tengah.add(barisStatus);
        tengah.add(Box.createVerticalStrut(10));

        JLabel penyewa;
        JLabel tempo;
        if (kamar.isTerisi()) {
            penyewa = UiUtil.label(kamar.getPenyewaAktif().getNama(), UiUtil.FONT_TEBAL, UiUtil.TEKS);
            tempo = UiUtil.label("Jatuh tempo: " + UiUtil.ringkasanJatuhTempo(kamar, hariIni),
                    UiUtil.FONT_KECIL, UiUtil.warnaTeks(statusTempo));
        } else {
            penyewa = UiUtil.label("Belum ada penyewa", UiUtil.FONT_NORMAL, UiUtil.TEKS_PUDAR);
            tempo = UiUtil.label(UiUtil.rupiah(kamar.getTarifPerBulan()) + " / bulan",
                    UiUtil.FONT_KECIL, UiUtil.TEKS_PUDAR);
        }
        penyewa.setAlignmentX(LEFT_ALIGNMENT);
        tempo.setAlignmentX(LEFT_ALIGNMENT);
        tengah.add(penyewa);
        tengah.add(Box.createVerticalStrut(3));
        tengah.add(tempo);
        return tengah;
    }

    private JPanel bangunBagianBawah(Listener listener) {
        UiUtil.Tombol detail = new UiUtil.Tombol("Detail", UiUtil.AKSEN_MUDA, UiUtil.AKSEN);
        detail.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        detail.addActionListener(e -> {
            listener.kamarDipilih(kamar);
            listener.kamarDibuka(kamar);
        });
        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        if (kamar.isBelumLunas()) {
            bawah.add(UiUtil.label("Sisa: " + UiUtil.rupiah(kamar.getTagihanAktif().getSisa()),
                    UiUtil.FONT_TEBAL, UiUtil.ORANYE), BorderLayout.WEST);
        }
        bawah.add(detail, BorderLayout.EAST);
        return bawah;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 1;
        int h = getHeight() - 1;
        g2.setColor(terpilih ? new Color(0xF5F9FF) : UiUtil.KARTU);
        g2.fillRoundRect(0, 0, w, h, RADIUS, RADIUS);
        if (terpilih) {
            g2.setColor(UiUtil.AKSEN);
            g2.setStroke(new java.awt.BasicStroke(2.5f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, RADIUS, RADIUS);
        } else {
            g2.setColor(UiUtil.GARIS);
            g2.drawRoundRect(0, 0, w, h, RADIUS, RADIUS);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
