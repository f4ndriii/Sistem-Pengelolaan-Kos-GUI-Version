# Product Requirements Document (PRD)

**Nama Produk:** Sistem Pengelolaan Kos (Desktop App)

**Versi:** 1.0 (MVP — Kolaborasi Tim)

**Target Pengguna:** Pemilik Kos

**Platform & Teknologi:** Java Desktop (Java Swing), OOP, dan `java.util.LinkedList`

---

## 1. Latar Belakang & Tujuan Produk

Pemilik kos sering mengalami kendala ketika melakukan pencatatan pengelolaan kos secara manual di buku fisik, seperti risiko catatan rusak/hilang, perhitungan biaya sewa yang lambat, serta kesulitan mencari data penyewa maupun memantau tanggal jatuh tempo pembayaran.

Aplikasi desktop ini bertujuan memberikan alat bantu pencatatan dan pemantauan internal bagi pemilik kos agar pengelolaan unit kamar, data penyewa, perhitungan biaya sewa, serta pencatatan pembayaran dan jatuh tempo dapat dilakukan lebih cepat, terstruktur, dan terpusat.

---

## 2. Ruang Lingkup (*Scope*)

### A. Termasuk dalam Ruang Lingkup (*In-Scope*)

* Pengelolaan unit kamar kos (tambah kamar berdasarkan tipe, lihat daftar kamar, kelompokkan/urutkan kamar, dan hapus kamar kosong).


* Pengelolaan penempatan penyewa pada kamar (tambah penyewa ke kamar kosong, cari penyewa, dan kosongkan kamar saat penyewa keluar).


* Perhitungan biaya sewa bulanan sesuai tipe kamar dan durasi sewa.


* Pencatatan pembayaran sewa dari sisi pemilik kos dan pemantauan tanggal jatuh tempo.


* Antarmuka grafis (GUI) berbasis **Java Swing** dengan penyimpanan koleksi data menggunakan **`java.util.LinkedList`**.

### B. Di Luar Ruang Lingkup (*Out-of-Scope*)

* **Tidak ada transaksi penyewaan mandiri oleh penyewa kos** (tidak ada akun penyewa, *booking* online, keranjang sewa, maupun *payment gateway*).
* Aplikasi beroperasi sebagai aplikasi desktop untuk satu aktor tunggal, yaitu **Pemilik Kos**.



---

## 3. Pengguna & Kebutuhan Utama

* **Aktor Utama:** Pemilik Kos


* **Kebutuhan Utama:**
1. Melihat kondisi seluruh kamar kos dalam satu tampilan visual yang jelas (mana yang kosong, mana yang terisi, siapa penyewanya, dan bagaimana status pembayarannya).


2. Menambah atau menghapus unit kamar sesuai kondisi kos.


3. Mendaftarkan penyewa baru ke kamar kosong dan mengosongkan kembali kamar saat penyewa berhenti menyewa.


4. Mencari penyewa dengan cepat dan mengurutkan tampilan kamar berdasarkan kriteria tertentu.


5. Menghitung total biaya sewa secara otomatis serta mencatat pembayaran dan memantau jatuh tempo tanpa menghitung manual.





---

## 4. Spesifikasi Fitur & Fungsionalitas

| Kode | Nama Fitur | Deskripsi & Aturan Bisnis | Prioritas |
| --- | --- | --- | --- |
| **F-01** | **Tambah Kamar** | Pemilik dapat menambahkan kamar baru dengan memilih tipe kamar (**Standard** atau **Premium**), mengisi nomor kamar, tarif dasar, serta biaya fasilitas tambahan (khusus tipe Premium). Nomor kamar tidak boleh duplikat.

 | Wajib (P0) |
| **F-02** | **Lihat & Kelompokkan Kamar** | Menampilkan daftar seluruh kamar dalam bentuk grid kartu kamar yang memuat nomor kamar, tipe kamar, status (`Kosong` / `Terisi`), nama penyewa (jika terisi), dan ringkasan jatuh tempo. Pemilik dapat mengelompokkan/mengurutkan berdasarkan jenis kamar atau kriteria di *Sort by*.

 | Wajib (P0) |
| **F-03** | **Hapus Kamar** | Pemilik dapat menghapus kamar yang dipilih. **Aturan:** Kamar hanya dapat dihapus jika berstatus `Kosong` agar data penyewa aktif tidak hilang secara tidak sengaja.

 | Wajib (P0) |
| **F-04** | **Tambah Penyewa ke Kamar** | Pemilik mengisi data penyewa (ID/NIK, nama, kontak) dan menempatkannya ke kamar yang berstatus `Kosong`. Status kamar otomatis berubah menjadi `Terisi`.

 | Wajib (P0) |
| **F-05** | **Kosongkan Kamar** | Pemilik dapat mengosongkan kamar yang sedang `Terisi` ketika penyewa keluar. Status kamar kembali menjadi `Kosong` dan kamar siap ditempati penyewa baru.

 | Wajib (P0) |
| **F-06** | **Cari Penyewa** | Pemilik mengetik kata kunci pada kolom pencarian di bagian atas untuk memfilter kamar/penyewa berdasarkan nama penyewa (atau nomor kamar) secara cepat.

 | Wajib (P0) |
| **F-07** | **Hitung Biaya Sewa** | Sistem menghitung total biaya sewa secara otomatis berdasarkan tipe kamar (`Standard` vs `Premium`) dikalikan durasi sewa (jumlah bulan). Ditampilkan pada form penempatan penyewa maupun dialog detail/pembayaran kamar.

 | Wajib (P0) |
| **F-08** | **Catat Pembayaran** | Melalui detail kamar yang terisi, pemilik dapat mencatat pembayaran sewa yang diterima (durasi bulan yang dibayar, nominal, tanggal bayar) ke dalam riwayat pembayaran dan memperbarui tanggal jatuh tempo berikutnya.

 | Wajib (P0) |
| **F-09** | **Cek Jatuh Tempo** | Sistem membandingkan tanggal jatuh tempo penyewa aktif dengan tanggal hari ini (`LocalDate`) untuk menandai status (*Aman*, *Mendekati Jatuh Tempo*, atau *Lewat Jatuh Tempo*) pada kartu kamar serta mendukung pengurutan (*Sort by*) berdasarkan jatuh tempo terdekat.

 | Wajib (P0) |

---

## 5. Rancangan Antarmuka (UI/UX Overview)

Mengacu pada sketsa awal antarmuka, layar utama (`MainFrame`) dibagi menjadi tiga area:

1. **Sidebar Kiri (Panel Aksi Utama):**
* Judul Aplikasi di bagian atas.


* Tombol aksi utama: **Tambah Kamar**, **Hapus Kamar**, **Tambah Penyewa ke Kamar**, dan **Kosongkan Kamar**.




2. **Header Atas Area Konten (Pencarian & Pengurutan):**
* Kolom teks **Cari berdasarkan nama penyewa** (filter langsung/tombol cari).


* Dropdown **Sort by** (opsi: *Nomor Kamar*, *Jenis Kamar (Standard/Premium)*, *Status Kosong/Terisi*, dan *Jatuh Tempo Terdekat*).




3. **Area Utama (Grid Kartu Kamar dalam `JScrollPane`):**
* Menampilkan unit kamar dalam bentuk kartu-kartu (grid 3 kolom yang dapat di-scroll jika jumlah kamar bertambah).


* Klik 1x pada kartu untuk memilih kamar (menandai kamar aktif untuk tombol *Hapus* atau *Kosongkan*).
* Klik 2x (atau tombol detail pada kartu) untuk membuka **Dialog Detail Kamar & Pembayaran** (berisi info penyewa, kalkulator biaya sewa, riwayat/form catat pembayaran, dan status jatuh tempo).



---

## 6. Spesifikasi Teknis & Arsitektur Kode

### A. Prinsip OOP & Struktur Data

* **Struktur Data:** Menggunakan `java.util.LinkedList` bawaan Java di dalam class pengelola (`KosManager`) untuk menyimpan koleksi `Kamar`, `Penyewa`, dan `Pembayaran`.
* **Penerapan OOP:**
* **Interface (`InfoDetail`):** Kontrak method untuk memformat informasi detail objek.
* **Abstract Class (`Kamar`):** Atribut umum (`nomorKamar`, `hargaDasar`, `statusTerisi`) dan *abstract method* `hitungHargaTotal(int jumlahBulan)`.
* **Inheritance & Polymorphism:** `KamarStandard` dan `KamarPremium` mewarisi `Kamar` dengan rumus perhitungan harga yang berbeda.
* **Encapsulation & Association:** Class `Penyewa` terhubung dengan `Kamar`, dan class `Pembayaran` terhubung dengan `Penyewa`/`Kamar`.



### B. Struktur Folder Repository (GitHub)

```text
sistem-pengelolaan-kos/
├── README.md
└── src/
    └── com/kos/
        ├── Main.java                      # Entry point (Swing EDT)
        ├── model/
        │   ├── InfoDetail.java            # Interface
        │   ├── Kamar.java                 # Abstract class
        │   ├── KamarStandard.java         # Subclass kamar standard
        │   ├── KamarPremium.java          # Subclass kamar premium
        │   ├── Penyewa.java               # Entitas penyewa
        │   └── Pembayaran.java            # Entitas catatan pembayaran
        ├── manager/
        │   └── KosManager.java            # Logika bisnis & LinkedList
        └── ui/
            ├── MainFrame.java             # Window utama (Sidebar, Topbar, Grid)
            ├── KamarCardPanel.java        # Komponen kartu per kamar
            └── DialogDetailKamar.java     # Dialog detail, hitung biaya & catat bayar

```

---

## 7. Pembagian Tugas Tim (Saran Kolaborasi)

Agar pengerjaan paralel di GitHub minim konflik, tugas dapat dibagi menjadi 3 jalur utama:

1. **Modul Model & Logika Data (`model/` & `manager/KosManager.java`):**
* Membuat seluruh class model OOP, struktur `LinkedList`, validasi kamar kosong/terisi, logika hitung biaya, pencarian, sorting, dan pengecekan jatuh tempo.


2. **Modul UI Utama (`ui/MainFrame.java` & `ui/KamarCardPanel.java`):**
* Membangun tampilan sidebar, topbar (search & sort), grid kartu kamar yang responsif di dalam `JScrollPane`, serta interaksi pemilihan kartu.


3. **Modul Form & Dialog Interaksi (`ui/DialogDetailKamar.java` & dialog input):**
* Membangun form *Tambah Kamar*, *Tambah Penyewa*, kalkulator biaya sewa, form *Catat Pembayaran*, dan menghubungkan seluruh aksi UI ke `KosManager`.



---

## 8. Kriteria Keberhasilan (*Acceptance Criteria*)

* Aplikasi dapat dijalankan tanpa *error* menggunakan Java Swing dan menyimpan seluruh data runtime di `java.util.LinkedList`.
* Pemilik kos dapat menambah kamar `Standard` dan `Premium` dengan perhitungan biaya bulanan yang berbeda dan akurat.
* Kamar yang sudah ditempati penyewa tidak dapat dihapus maupun diisi penyewa lain sebelum dikosongkan.
* Pencarian nama penyewa dan fitur *Sort by* memperbarui tampilan grid kartu kamar secara akurat tanpa merusak data utama.
* Pemilik kos dapat mencatat pembayaran untuk penyewa aktif dan melihat status jatuh tempo pada kartu/detail kamar.

---