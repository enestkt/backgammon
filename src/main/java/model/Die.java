package model;

import java.util.Random;

/**
 * Die sınıfı: 1 ile 6 arasında rastgele zar değeri üretir.
 */
public class Die {
    private final Random random;
    private int value;

    public Die() {
        random = new Random();
        roll(); // başlangıçta hemen bir değer ver
    }

    /**
     * Zar atılır ve yeni değer üretilir.
     */
    public void roll() {
        value = random.nextInt(6) + 1;
    }

    /**
     * Şu anki zar değerini döner.
     */
    public int getValue() {
        return value;
    }
}
