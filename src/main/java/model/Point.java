package model;

import java.io.Serializable;

/**
 * Point sınıfı, tavla tahtası üzerindeki tek bir noktayı (slot'u) temsil eder.
 * Noktadaki taşların rengi, sayısı ve o noktayla ilgili çeşitli kontroller burada yapılır.
 */
public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    // Noktanın tahtadaki index'i (0-23 arası)
    private final int index;

    // Noktadaki taşların rengi (boşsa null)
    private Color color;

    // Noktadaki taş sayısı
    private int count;

    /**
     * Point yapıcı metodu.
     * @param index Noktanın index'i
     */
    public Point(int index) {
        this.index = index;
        this.color = null;
        this.count = 0;
    }

    public int getIndex() {
        return index;
    }

    public Color getColor() {
        return color;
    }

    public int getCount() {
        return count;
    }

    /**
     * Nokta tamamen boş mu?
     * @return Taş yoksa true
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Bu nokta verilen renkteki oyuncuya mı ait?
     * @param c Renk
     * @return Taş varsa ve rengi uyuyorsa true
     */
    public boolean isOwnedBy(Color c) {
        return !isEmpty() && color == c;
    }

    /**
     * Bu nokta, verilen renkten oyuncuya bloklu mu?
     * Yani rakipten en az 2 taş varsa true.
     * @param c Kontrol edilecek oyuncu rengi
     * @return Blok varsa true
     */
    public boolean isBlockedFor(Color c) {
        return !isEmpty() && color != c && count >= 2;
    }

    /**
     * Bu noktaya yeni bir taş ekler.
     * Rakipten 1 taş varsa onu kırar. 6'dan fazla taş koyulamaz.
     * @param color Taşın rengi
     */
    public void addChecker(Color color) {
        if (!isEmpty() && this.color != color && count > 1) {
            throw new IllegalStateException("Cannot place checker on opponent's point with more than 1 checker.");
        }

        if (count >= 6) {
            throw new IllegalStateException("Bu noktada maksimum 6 taş olabilir.");
        }

        if (!isEmpty() && this.color != color && count == 1) {
            removeChecker();
            System.out.println("Rakip taşı kırdın!");
        }

        this.color = color;
        count++;
    }

    /**
     * Bu noktadan bir taş eksiltir. Son taş da çıkarılırsa renk null olur.
     */
    public void removeChecker() {
        if (count > 0) {
            count--;
            if (count == 0) {
                color = null;
            }
        } else {
            throw new IllegalStateException("No checkers to remove.");
        }
    }

    /**
     * Noktadaki taş sayısı ve rengi hızlıca ayarlanır.
     * Pozitif: WHITE, Negatif: BLACK, 0: boş
     * @param value Ayarlanacak değer
     */
    public void setCountAndColor(int value) {
        if (value == 0) {
            this.setCount(0);
            this.setColor(null); // veya boş renk
        } else if (value > 0) {
            this.setCount(value);
            this.setColor(Color.WHITE);
        } else {
            this.setCount(-value);
            this.setColor(Color.BLACK);
        }
    }

    // setCount ve setColor metodlarının içi implement edilmemiş! Kullanılırsa hata verir.
    private void setCount(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void setColor(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
