package com.kos.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


final class Json {

    private Json() {
    }

    static String tulis(Object nilai) {
        StringBuilder sb = new StringBuilder();
        tulis(sb, nilai, 0);
        sb.append('\n');
        return sb.toString();
    }

    private static void tulis(StringBuilder sb, Object nilai, int tingkat) {
        if (nilai == null) {
            sb.append("null");
        } else if (nilai instanceof String s) {
            tulisString(sb, s);
        } else if (nilai instanceof Boolean b) {
            sb.append(b.booleanValue());
        } else if (nilai instanceof Number n) {
            tulisAngka(sb, n.doubleValue());
        } else if (nilai instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                indent(sb, tingkat + 1);
                tulisString(sb, String.valueOf(e.getKey()));
                sb.append(": ");
                tulis(sb, e.getValue(), tingkat + 1);
                sb.append(++i < map.size() ? ",\n" : "\n");
            }
            indent(sb, tingkat);
            sb.append('}');
        } else if (nilai instanceof List<?> list) {
            if (list.isEmpty()) {
                sb.append("[]");
                return;
            }
            sb.append("[\n");
            for (int i = 0; i < list.size(); i++) {
                indent(sb, tingkat + 1);
                tulis(sb, list.get(i), tingkat + 1);
                sb.append(i < list.size() - 1 ? ",\n" : "\n");
            }
            indent(sb, tingkat);
            sb.append(']');
        } else {
            throw new IllegalArgumentException("Tipe tidak didukung: " + nilai.getClass());
        }
    }

    private static void tulisAngka(StringBuilder sb, double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("Angka tidak valid: " + d);
        }
        if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            sb.append((long) d);
        } else {
            sb.append(d);
        }
    }

    private static void tulisString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static void indent(StringBuilder sb, int tingkat) {
        for (int i = 0; i < tingkat; i++) {
            sb.append("  ");
        }
    }

    static Object baca(String teks) throws IOException {
        Pembaca p = new Pembaca(teks);
        p.lewatiSpasi();
        Object hasil = p.nilai();
        p.lewatiSpasi();
        if (p.pos < teks.length()) {
            throw p.salah("Ada teks berlebih setelah akhir JSON");
        }
        return hasil;
    }

    private static final class Pembaca {
        private final String s;
        private int pos;

        Pembaca(String s) {
            // buang BOM bila ada
            this.s = s.startsWith("\uFEFF") ? s.substring(1) : s;
        }

        IOException salah(String pesan) {
            return new IOException(pesan + " (posisi " + pos + ")");
        }

        void lewatiSpasi() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        Object nilai() throws IOException {
            if (pos >= s.length()) {
                throw salah("JSON berakhir terlalu cepat");
            }
            char c = s.charAt(pos);
            switch (c) {
                case '{':
                    return objek();
                case '[':
                    return array();
                case '"':
                    return string();
                case 't':
                    return kata("true", Boolean.TRUE);
                case 'f':
                    return kata("false", Boolean.FALSE);
                case 'n':
                    return kata("null", null);
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return angka();
                    }
                    throw salah("Karakter tidak dikenal '" + c + "'");
            }
        }

        private Object kata(String kata, Object hasil) throws IOException {
            if (!s.startsWith(kata, pos)) {
                throw salah("Token tidak valid");
            }
            pos += kata.length();
            return hasil;
        }

        private Map<String, Object> objek() throws IOException {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // {
            lewatiSpasi();
            if (pos < s.length() && s.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                lewatiSpasi();
                if (pos >= s.length() || s.charAt(pos) != '"') {
                    throw salah("Nama properti harus berupa string");
                }
                String kunci = string();
                lewatiSpasi();
                if (pos >= s.length() || s.charAt(pos) != ':') {
                    throw salah("Tanda ':' tidak ditemukan");
                }
                pos++;
                lewatiSpasi();
                map.put(kunci, nilai());
                lewatiSpasi();
                if (pos >= s.length()) {
                    throw salah("Objek tidak ditutup");
                }
                char c = s.charAt(pos++);
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    throw salah("Diharapkan ',' atau '}'");
                }
            }
        }

        private List<Object> array() throws IOException {
            List<Object> list = new ArrayList<>();
            pos++; // [
            lewatiSpasi();
            if (pos < s.length() && s.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                lewatiSpasi();
                list.add(nilai());
                lewatiSpasi();
                if (pos >= s.length()) {
                    throw salah("Array tidak ditutup");
                }
                char c = s.charAt(pos++);
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    throw salah("Diharapkan ',' atau ']'");
                }
            }
        }

        private String string() throws IOException {
            StringBuilder sb = new StringBuilder();
            pos++; // "
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (pos >= s.length()) {
                    break;
                }
                char e = s.charAt(pos++);
                switch (e) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (pos + 4 > s.length()) {
                            throw salah("Escape \\u tidak lengkap");
                        }
                        try {
                            sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw salah("Escape \\u tidak valid");
                        }
                        pos += 4;
                    }
                    default -> throw salah("Escape tidak valid '\\" + e + "'");
                }
            }
            throw salah("String tidak ditutup");
        }

        private Double angka() throws IOException {
            int awal = pos;
            if (s.charAt(pos) == '-') {
                pos++;
            }
            while (pos < s.length() && "0123456789.eE+-".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            try {
                return Double.valueOf(s.substring(awal, pos));
            } catch (NumberFormatException e) {
                pos = awal;
                throw salah("Angka tidak valid");
            }
        }
    }
}
