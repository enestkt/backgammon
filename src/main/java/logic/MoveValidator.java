package logic;

import model.*;

/**
 * MoveValidator sınıfı, tavla oyununda hamlelerin geçerli olup olmadığını kontrol eden yardımcı bir sınıftır.
 * Hamle doğrulama, taş çıkarma (bear off) ve ev bölgesi kontrollerini sağlar.
 */
public class MoveValidator {

    // Oyun tahtasını tutan Board nesnesi
    private Board board;

    /**
     * MoveValidator yapıcı metodu.
     * @param board Doğrulama işlemlerinde kullanılacak oyun tahtası nesnesi
     */
    public MoveValidator(Board board) {
        this.board = board;
    }

    /**
     * Belirtilen hamlenin (move) ve oyuncunun, kurallara uygun olup olmadığını kontrol eder.
     * Aşağıdaki kontrolleri yapar:
     * - Bar'da taş varsa başka taş oynanamaz
     * - Hedef alan geçerli mi (tahta sınırları içinde mi)
     * - Hedef noktada rakip bloku var mı
     * - Bar'dan hamle yapılıyorsa, from -1 olmalı
     * - Normal hamlede taşın oyuncuya ait olup olmadığı
     *
     * @param move   Hamle bilgisi (başlangıç, bitiş, bar'dan mı vs)
     * @param player Hamleyi yapan oyuncu
     * @return Hamle geçerliyse true, değilse false
     */
    public boolean isValidMove(Move move, Player player) {
        Color color = player.getColor();

        int from = move.getFrom();
        int to = move.getTo();

        // 1. Bar kontrolü: Bar'da taş varken başka taş oynanamaz
        if (board.getBarCount(color) > 0 && !move.isFromBar()) {
            return false;
        }

        // 2. Hedef (to) aralığı kontrolü: Tahta sınırları
        if (to < 0 || to > 23) {
            return false;
        }

        Point destination = board.getPoint(to);

        // 3. Hedef nokta rakip tarafından bloklu mu?
        if (destination.isBlockedFor(color)) {
            return false;
        }

        // 4. Bar'dan geliyorsa from = -1 olmalı, hedef bloklu değilse geçerli
        if (move.isFromBar()) {
            return !destination.isBlockedFor(color);
        }

        // 5. Normal taş oynama: Başlangıç noktası geçerli mi?
        if (from < 0 || from > 23) {
            return false;
        }

        Point origin = board.getPoint(from);
        if (!origin.isOwnedBy(color)) {
            return false;
        }

        // Tüm kontroller geçtiyse hamle geçerli
        return true;
    }

    /**
     * Belirli bir oyuncunun, belirli bir taştan ve zar değerine göre taş çıkarma (bear off) yapıp yapamayacağını kontrol eder.
     * Sadece tüm taşlar ev bölgesindeyse taş çıkarılabilir.
     *
     * @param player     Oyuncu
     * @param fromIndex  Taşın bulunduğu nokta
     * @param diceValue  Kullanılan zar değeri
     * @return Eğer taş çıkarılabiliyorsa true, aksi halde false
     */
    public boolean isBearOffPossible(Player player, int fromIndex, int diceValue) {
        Color color = player.getColor();
        if (!areAllCheckersInHomeBoard(color)) return false;

        // Beyaz taşlar 24'ten büyükse, siyah taşlar 0'dan küçükse çıkarılabilir
        if (color == Color.WHITE) {
            return fromIndex + diceValue >= 24;
        } else {
            return fromIndex - diceValue < 0;
        }
    }

    /**
     * Belirli bir renkteki oyuncunun tüm taşlarının kendi ev bölgesinde olup olmadığını kontrol eder.
     * Ev bölgesinde olmayan taş varsa false döner.
     *
     * @param color Kontrol edilecek oyuncu rengi
     * @return Tüm taşlar evdeyse true, değilse false
     */
    private boolean areAllCheckersInHomeBoard(Color color) {
        int start = (color == Color.WHITE) ? 18 : 0;
        int end = (color == Color.WHITE) ? 23 : 5;

        for (int i = 0; i < 24; i++) {
            Point p = board.getPoint(i);
            // Taş ev bölgesi dışında ise false döner
            if (p.getColor() == color && (i < start || i > end)) {
                return false;
            }
        }
        return true;
    }
}
