package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Player sınıfı, tavla oyunundaki bir oyuncunun bilgilerini tutar.
 * Oyuncunun adı, rengi, bar ve çıkarılan taş sayıları ile kalan hamle değerlerini saklar.
 */
public class Player {
    // Oyuncunun adı
    private String name;

    // Oyuncunun taş rengi
    private Color color;

    // Dışarı çıkarılan taş sayısı
    private int borneOffCount;

    // Bar'daki taş sayısı (kırık)
    private int barCount;

    // O anki zarlarla oynanabilir hamle değerleri
    private List<Integer> moveValues = new ArrayList<>();

    /**
     * Player yapıcı metodu.
     * @param name Oyuncu adı
     * @param color Oyuncunun rengi
     */
    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.borneOffCount = 0;
        this.barCount = 0;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getBorneOffCount() {
        return borneOffCount;
    }

    /**
     * Dışarı çıkarılan taş sayısını bir artırır.
     */
    public void incrementBorneOff() {
        borneOffCount++;
    }

    /**
     * Oyuncu tüm taşlarını dışarı çıkardı mı?
     * @return 15 taş çıkardıysa true
     */
    public boolean hasWon() {
        return borneOffCount == 15;
    }

    public int getBarCount() {
        return barCount;
    }

    /**
     * Bar'daki taş sayısını bir artırır.
     */
    public void incrementBar() {
        barCount++;
    }

    /**
     * Bar'daki taş sayısını bir azaltır.
     */
    public void decrementBar() {
        if (barCount > 0) barCount--;
    }

    /**
     * O an oynanabilir hamle değerlerini döndürür.
     * @return moveValues listesi
     */
    public List<Integer> getMoveValues() {
        return moveValues;
    }

    /**
     * O anki hamle değerleri listesini ayarla.
     * @param values Yeni hamle değerleri
     */
    public void setMoveValues(List<Integer> values) {
        moveValues.clear();
        moveValues.addAll(values);
    }
}
