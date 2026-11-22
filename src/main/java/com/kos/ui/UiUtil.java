package com.kos.ui;

import com.kos.model.Kamar;
import com.kos.model.StatusJatuhTempo;
import java.awt.*;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;
import java.awt.event.*;

public final class UiUtil {

    private UiUtil() {
    }

    public static final Color SIDEBAR = new Color(0x1E2A44);
    public static final Color LATAR = new Color(0xF3F5F9);
    public static final Color KARTU = Color.WHITE;
    public static final Color GARIS = new Color(0xD5DAE3);
    public static final Color TEKS = new Color(0x1F2937);
    public static final Color TEKS_PUDAR = new Color(0x6B7280);
    public static final Color AKSEN = new Color(0x2563EB);
    public static final Color AKSEN_MUDA = new Color(0xEAF1FF);
    public static final Color BAHAYA = new Color(0xDC2626);

    public static final Color HIJAU = new Color(0x15803D);
    public static final Color HIJAU_MUDA = new Color(0xDCFCE7);
    public static final Color KUNING = new Color(0xB45309);
    public static final Color KUNING_MUDA = new Color(0xFEF3C7);
    public static final Color MERAH = new Color(0xB91C1C);
    public static final Color MERAH_MUDA = new Color(0xFEE2E2);
    public static final Color ABU = new Color(0x4B5563);
    public static final Color ABU_MUDA = new Color(0xE5E7EB);
    public static final Color BIRU = new Color(0x1D4ED8);
    public static final Color BIRU_MUDA = new Color(0xDBEAFE);
    public static final Color UNGU = new Color(0x7E22CE);
    public static final Color UNGU_MUDA = new Color(0xF3E8FF);
    public static final Color ORANYE = new Color(0xC2410C);
    public static final Color ORANYE_MUDA = new Color(0xFFEDD5);

    public static final Font FONT_NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_TEBAL = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_JUDUL = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    public static final Font FONT_KECIL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final DecimalFormat FORMAT_RUPIAH =
            new DecimalFormat("#,##0", new DecimalFormatSymbols(INDONESIA));
    private static final DateTimeFormatter FORMAT_WAKTU =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", INDONESIA);
    private static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("d MMM yyyy", INDONESIA);

    public static void aturLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            // Tampilan bawaan Swing tetap bisa dipakai.
        }
    }

    public static String rupiah(double nilai) {
        return "Rp " + FORMAT_RUPIAH.format(Math.round(nilai));
    }

    public static String waktu(LocalDateTime waktu) {
        return FORMAT_WAKTU.format(waktu);
    }

    public static String tanggal(LocalDate tanggal) {
        return tanggal == null ? "-" : FORMAT_TANGGAL.format(tanggal);
    }

    public static double parseRupiah(String teks, String namaIsian) {
        String angka = teks == null ? "" : teks.replaceAll("[^0-9]", "");
        if (angka.isEmpty()) {
            throw new NumberFormatException(namaIsian + " wajib diisi dengan angka.");
        }
        if (angka.length() > 12) {
            throw new NumberFormatException(namaIsian + " terlalu besar.");
        }
        return Double.parseDouble(angka);
    }

    public static LocalDate keLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date keDate(LocalDate tanggal) {
        return Date.from(tanggal.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static JSpinner spinnerTanggal(LocalDate awal) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(keDate(awal), null, null,
                java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd-MM-yyyy"));
        return spinner;
    }

    public static LocalDate ambilTanggal(JSpinner spinner) {
        return keLocalDate((Date) spinner.getValue());
    }

    public static String ringkasanJatuhTempo(Kamar kamar, LocalDate hariIni) {
        if (!kamar.isTerisi() || kamar.getTanggalJatuhTempo() == null) {
            return "-";
        }
        long selisih = ChronoUnit.DAYS.between(hariIni, kamar.getTanggalJatuhTempo());
        String tgl = tanggal(kamar.getTanggalJatuhTempo());
        if (selisih < 0) {
            return tgl + " (terlambat " + (-selisih) + " hari)";
        }
        if (selisih == 0) {
            return tgl + " (hari ini)";
        }
        return tgl + " (" + selisih + " hari lagi)";
    }

    public static Color warnaTeks(StatusJatuhTempo status) {
        if (status == null) {
            return ABU;
        }
        switch (status) {
            case LEWAT:
                return MERAH;
            case MENDEKATI:
                return KUNING;
            default:
                return HIJAU;
        }
    }

    public static Color warnaLatar(StatusJatuhTempo status) {
        if (status == null) {
            return ABU_MUDA;
        }
        switch (status) {
            case LEWAT:
                return MERAH_MUDA;
            case MENDEKATI:
                return KUNING_MUDA;
            default:
                return HIJAU_MUDA;
        }
    }

    public static void pesanError(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Tidak dapat diproses", JOptionPane.ERROR_MESSAGE);
    }

    public static void pesanInfo(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean konfirmasi(Component induk, String judul, String pesan) {
        return JOptionPane.showConfirmDialog(induk, pesan, judul, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }


    public static JLabel label(String teks, Font font, Color warna) {
        JLabel label = new JLabel(teks);
        label.setFont(font);
        label.setForeground(warna);
        return label;
    }

    public static class Lencana extends JLabel {
        private Color latar;

        public Lencana(String teks, Color warnaTeks, Color latar) {
            super(teks, CENTER);
            this.latar = latar;
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            setForeground(warnaTeks);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        }

        public void atur(String teks, Color warnaTeks, Color latar) {
            setText(teks);
            setForeground(warnaTeks);
            this.latar = latar;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(latar);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class Tombol extends JButton {
        private final Color warna;
        private final Color warnaTeks;
        private boolean hover;

        public Tombol(String teks, Color warna, Color warnaTeks) {
            super(teks);
            this.warna = warna;
            this.warnaTeks = warnaTeks;
            setFont(FONT_TEBAL);
            setForeground(warnaTeks);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color isi = !isEnabled() ? ABU_MUDA : (hover ? warna.brighter() : warna);
            g2.setColor(isi);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(isEnabled() ? warnaTeks : TEKS_PUDAR);
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 32, fm.getHeight() + 18);
        }
    }
}
