package com.kos.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum StatusJatuhTempo {
    AMAN("Aman"),
    MENDEKATI("Mendekati Jatuh Tempo"),
    LEWAT("Lewat Jatuh Tempo");

    public static final int BATAS_MENDEKATI_HARI = 7;

    private final String label;

    StatusJatuhTempo(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static StatusJatuhTempo hitung(LocalDate jatuhTempo, LocalDate hariIni) {
        if (jatuhTempo == null) {
            return null;
        }
        long sisaHari = ChronoUnit.DAYS.between(hariIni, jatuhTempo);
        if (sisaHari < 0) {
            return LEWAT;
        }
        if (sisaHari <= BATAS_MENDEKATI_HARI) {
            return MENDEKATI;
        }
        return AMAN;
    }

    @Override
    public String toString() {
        return label;
    }
}
