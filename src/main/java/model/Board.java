package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Board sınıfı, tavla oyun tahtasını ve taşların yerleşimini tutar.
 * Tahta üzerindeki noktalar, bar (kırılmış taşlar) ve dışarı çıkarılan taşlar bu sınıfta yönetilir.
 */
public class Board implements Serializable {

    // Sürüm uyumluluğu için gerekli sabit
    private static final long serialVersionUID = 1L;

    // 24 adet nokta (taşların bulunduğu yerler) tutan dizi
    private Point[] points;

    // Her oyuncunun bar'daki (kırılmış) taş sayısını tutar
    private Map<Color, Integer> bar;

    // Her oyuncunun dışarı çıkardığı taşların sayısını tutar
    private Map<Color, Integer> borneOff;

    /**
     * Board yapıcı metodu.
     * Tahta üzerindeki tüm noktaları, bar ve borneOff haritalarını başlatır ve taşları başlangıç dizilimine yerleştirir.
     */
    public Board() {
        points = new Point[24];
        for (int i = 0; i < 24; i++) {
            points[i] = new Point(i);
        }

        bar = new HashMap<>();
        bar.put(Color.WHITE, 0);
        bar.put(Color.BLACK, 0);

        borneOff = new HashMap<>();
        borneOff.put(Color.WHITE, 0);
        borneOff.put(Color.BLACK, 0);

        initializeStartingPosition();
    }

    /**
     * Tahtayı oyunun başlangıç pozisyonuna göre taşlarla doldurur.
     * Tavla kurallarına uygun olarak taşlar yerleştirilir.
     */
    private void initializeStartingPosition() {
        // ÜST KISIM
        placeCheckers(0, Color.WHITE, 5);   // sol en üst
        placeCheckers(4, Color.BLACK, 3);
        placeCheckers(6, Color.BLACK, 5);
        placeCheckers(11, Color.WHITE, 2);  // sağ en üst

        // ALT KISIM
        placeCheckers(12, Color.BLACK, 2);  // sağ en alt
        placeCheckers(17, Color.WHITE, 5);
        placeCheckers(19, Color.WHITE, 3);
        placeCheckers(23, Color.BLACK, 5);  // sol en alt
    }

    /**
     * Belirtilen noktaya istenen renkte ve sayıda taş yerleştirir.
     * @param index Nokta indeksi (0-23)
     * @param color Taş rengi
     * @param count Yerleştirilecek taş sayısı
     */
    private void placeCheckers(int index, Color color, int count) {
        for (int i = 0; i < count; i++) {
            points[index].addChecker(color);
        }
    }

    /**
     * İstenilen indeksdeki Point (noktayı) döndürür.
     * @param index 0-23 arası tahta noktası
     * @return İstenen noktanın Point nesnesi
     */
    public Point getPoint(int index) {
        return points[index];
    }

    /**
     * Belirtilen renkte oyuncunun bar'daki (kırılmış) taş sayısını döndürür.
     * @param color Oyuncu rengi
     * @return Bar'daki taş sayısı
     */
    public int getBarCount(Color color) {
        return bar.get(color);
    }

    /**
     * Bar'a (kırık alana) bir taş ekler.
     * @param color Hangi oyuncunun taşı kırıldıysa
     */
    public void addToBar(Color color) {
        bar.put(color, bar.get(color) + 1);
    }

    /**
     * Bar'dan bir taş çıkarır (tahta üzerine girişte kullanılır).
     * @param color Hangi oyuncunun barından çıkıyor
     */
    public void removeFromBar(Color color) {
        bar.put(color, bar.get(color) - 1);
    }

    /**
     * Dışarı çıkarılan (borne off) taş sayısını döndürür.
     * @param color Oyuncu rengi
     * @return Dışarı çıkarılmış taş sayısı
     */
    public int getBorneOff(Color color) {
        return borneOff.get(color);
    }

    /**
     * Bir taşın oyundan dışarı çıkmasını (borne off) sağlar.
     * @param color Hangi oyuncunun taşı çıkıyor
     */
    public void bearOff(Color color) {
        borneOff.put(color, borneOff.get(color) + 1);
    }

    /**
     * Tahta üzerindeki tüm noktaları ve taş durumlarını konsola yazdırır.
     * Sadece debug/test amaçlıdır.
     */
    public void printBoard() {
        System.out.println("------ Tavla Tahtası ------");
        for (int i = 0; i < 24; i++) {
            Point p = points[i];
            String color = p.getColor() != null ? p.getColor().toString() : "empty";
            int count = p.getCount();
            System.out.printf("%2d: %s (%d)%n", i, color, count);
        }
        System.out.println("---------------------------");
    }
}
