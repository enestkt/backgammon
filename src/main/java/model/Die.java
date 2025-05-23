package model;

import java.util.Random;

/**
 * Die sınıfı, tavla oyununda kullanılan tek bir zarı temsil eder.
 * Zarın değeri ve rastgele atılmasını sağlar.
 */
public class Die {

    // Zarın güncel değeri (1-6 arası)
    private int value;

    // Rastgele sayı üretmek için Random nesnesi
    private final Random random;

    /**
     * Die yapıcı metodu. Başlangıçta zar değerini atar.
     */
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
     * Zarın mevcut değerini döndürür.
     * @return Zar değeri (1-6 arası)
     */
    public int getValue() {
        return value;
    }

    /**
     * Zarın değerini manuel olarak ayarla.
     * Özellikle sunucu/istemci iletişiminde veya testte kullanılır.
     * @param value Ayarlanacak değer (1-6 arası)
     */
    public void setValue(int value) {
        if (value >= 1 && value <= 6) {
            this.value = value;
        } else {
            throw new IllegalArgumentException("Geçersiz zar değeri: " + value);
        }
    }
}
