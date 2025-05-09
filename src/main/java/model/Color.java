package model;

public enum Color {
    WHITE,
    BLACK,
    LIGHT_GRAY,
    DARK_GRAY,
    ORANGE;

    public static Color fromString(String value) {
        return switch (value.toLowerCase()) {
            case "white" -> WHITE;
            case "black" -> BLACK;
            case "light_gray" -> LIGHT_GRAY;
            case "dark_gray" -> DARK_GRAY;
            case "orange" -> ORANGE;
            default -> throw new IllegalArgumentException("Unknown color: " + value);
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case WHITE -> "white";
            case BLACK -> "black";
            case LIGHT_GRAY -> "light_gray";
            case DARK_GRAY -> "dark_gray";
            case ORANGE -> "orange";
        };
    }
}
