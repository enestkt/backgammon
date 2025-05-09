package model;

public class Point {

    private final int index;
    private Color color;
    private int count;

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

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean isOwnedBy(Color c) {
        return !isEmpty() && color == c;
    }

    public boolean isBlockedFor(Color c) {
        return !isEmpty() && color != c && count >= 2;
    }

    /**
     * Taşı bu noktaya ekler. Rakip tek taş varsa kırılır, 2+ taş varsa hata
     * fırlatır.
     */
    /**
     * Belirtilen renkte bir taşı bu noktaya ekler. - Rakibin iki veya daha
     * fazla taşı varsa, hamle reddedilir. - Bu noktada zaten 6 taş varsa, hamle
     * reddedilir. - Rakip tek taş varsa, kırılır (bara gönderilmek üzere
     * kaldırılır). - Aksi halde taş bu noktaya eklenir.
     *
     * @param color Eklenmek istenen taşın rengi
     * @throws IllegalStateException Geçersiz hamle durumunda
     */
    public void addChecker(Color color) {
        // Eğer noktada rakibin 2 veya daha fazla taşı varsa, buraya gidilemez
        if (!isEmpty() && this.color != color && count > 1) {
            throw new IllegalStateException("Cannot place checker on opponent's point with more than 1 checker.");
        }

        // Eğer noktada zaten 6 taş varsa, daha fazla eklenemez
        if (count >= 6) {
            throw new IllegalStateException("Bu noktada maksimum 6 taş olabilir.");
        }

        // Rakipten yalnızca 1 taş varsa, bu taş kırılır (tahtadan kaldırılır)
        if (!isEmpty() && this.color != color && count == 1) {
            removeChecker(); // rakip taşı kaldır
            System.out.println("Rakip taşı kırdın!");
        }

        // Taşı ekle: renk ata ve sayıyı artır
        this.color = color;
        count++;
    }

    /**
     * Bu noktadan bir taşı çıkarır.
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
}
