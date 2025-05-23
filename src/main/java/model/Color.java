package model;

import java.io.Serializable;

/**
 * Color enum'u, tavla taşlarının ve noktalarının rengini tutmak için kullanılır.
 * Oyuncu rengi, taş rengi veya özel durumlar için tanımlar içerir.
 */
public enum Color implements Serializable {
    WHITE,
    BLACK,
    LIGHT_GRAY,
    DARK_GRAY,
    ORANGE,
    GRAY,
    CYAN;

    /**
     * String değeri uygun Color enum'una çevirir.
     * @param value Renk ismi (örn: "white")
     * @return Color nesnesi
     */
    public static Color fromString(String value) {
        return switch (value.toLowerCase()) {
            case "white" -> WHITE;
            case "black" -> BLACK;
            case "light_gray" -> LIGHT_GRAY;
            case "dark_gray" -> DARK_GRAY;
            case "orange" -> ORANGE;
            case "gray" -> GRAY;
            case "cyan" -> CYAN;
            default -> throw new IllegalArgumentException("Unknown color: " + value);
        };
    }

    /**
     * Enum'u string olarak döndürür (örn: "white")
     * @return Renk adı küçük harflerle
     */
    @Override
    public String toString() {
        return switch (this) {
            case WHITE -> "white";
            case BLACK -> "black";
            case LIGHT_GRAY -> "light_gray";
            case DARK_GRAY -> "dark_gray";
            case ORANGE -> "orange";
            case GRAY -> "gray";
            case CYAN -> "cyan";
        };
    }
}
