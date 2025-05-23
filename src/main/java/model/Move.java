package model;

/**
 * Move sınıfı, bir oyuncunun yaptığı hamleyi temsil eder.
 * Taşın başlangıç ve bitiş noktaları, oyuncunun rengi ve hamlede kırma (hit) olup olmadığı tutulur.
 */
public class Move {
    // Taşın geldiği nokta (index)
    private int from;

    // Taşın gittiği nokta (index)
    private int to;

    // Hamleyi yapan oyuncunun rengi
    private Color playerColor;

    // Rakip taş kırıldıysa true
    private boolean isHit;

    /**
     * Move yapıcı metodu.
     * @param from Başlangıç noktası (index)
     * @param to Bitiş noktası (index)
     * @param playerColor Oyuncunun rengi
     */
    public Move(int from, int to, Color playerColor) {
        this.from = from;
        this.to = to;
        this.playerColor = playerColor;
        this.isHit = false;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        this.isHit = hit;
    }

    /**
     * Bu hamlenin bar'dan çıkış olup olmadığını kontrol eder.
     * @return Eğer taş bar'dan çıkıyorsa true
     */
    public boolean isFromBar() {
        return from == -1;
    }

    /**
     * Bu hamlenin taş çıkarma (bear off) olup olmadığını kontrol eder.
     * @return Eğer taş çıkışı ise true
     */
    public boolean isToBearOff() {
        return to == 24 || to == -1;
    }

    @Override
    public String toString() {
        return "Move{" +
                "from=" + from +
                ", to=" + to +
                ", playerColor=" + playerColor +
                ", hit=" + isHit +
                '}';
    }
}
