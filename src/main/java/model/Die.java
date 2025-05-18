package model;

import java.util.Random;

public class Die {

    private int value;
    private final Random random;

    public Die() {
        random = new Random();
        roll(); // Başlangıçta bir değer ata
    }

    /**
     * Zarı at ve rastgele bir değer belirle (1-6 arası).
     */
    public void roll() {
        value = random.nextInt(6) + 1;
    }

    /**
     * Zarın değerini döndür.
     */
    public int getValue() {
        return value;
    }

    /**
     * Zarın değerini elle ayarla (özellikle ağ üzerinden gelen veriler için).
     */
    public void setValue(int value) {
        if (value >= 1 && value <= 6) {
            this.value = value;
        } else {
            throw new IllegalArgumentException("Geçersiz zar değeri: " + value);
        }
    }
}
