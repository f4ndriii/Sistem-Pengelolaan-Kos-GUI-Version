package com.kos.model;

import java.time.LocalDateTime;

public class ItemSampah {
    private final Kamar kamar;
    private final LocalDateTime waktuDihapus;

    public ItemSampah(Kamar kamar, LocalDateTime waktuDihapus) {
        this.kamar = kamar;
        this.waktuDihapus = waktuDihapus;
    }

    public Kamar getKamar() {
        return kamar;
    }

    public LocalDateTime getWaktuDihapus() {
        return waktuDihapus;
    }
}
