package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import model.Board;
import model.Color;
import model.Player;
import model.Point;
import network.MultiClientClient;

/**
 * GameManager sınıfı, tavla oyununun tüm mantığını ve akışını yöneten ana kontrol sınıfıdır.
 * Oyuncular, zarlar, hamle sırası ve taşların hareketini yönetir.
 * Sunucu-istemci mesajlaşmasını ve oyunun genel durumunu günceller.
 */
public class GameManager {

    // Oyun tahtasını temsil eden Board nesnesi
    private Board board;

    // Beyaz oyuncu nesnesi
    private Player whitePlayer;

    // Siyah oyuncu nesnesi
    private Player blackPlayer;

    // Sıradaki (hamle yapan) oyuncu
    private Player currentPlayer;

    // Zar yönetimi için DiceManager nesnesi
    private DiceManager diceManager;

    // O anda kalan oynanabilir zar değerleri listesi
    private List<Integer> moveValues = new ArrayList<>();

    // Zar atılıp atılmadığını belirten bayrak
    private boolean diceRolled = false;

    // Sunucuya bağlantı sağlayan istemci nesnesi (Client tarafı için)
    private MultiClientClient client;

    /**
     * GameManager yapıcı metodu. Oyuna iki oyuncu adı ile başlar ve gerekli nesneleri oluşturur.
     * @param whiteName Beyaz oyuncunun adı
     * @param blackName Siyah oyuncunun adı
     */
    public GameManager(String whiteName, String blackName) {
        this.whitePlayer = new Player(whiteName, Color.WHITE);
        this.blackPlayer = new Player(blackName, Color.BLACK);
        this.currentPlayer = whitePlayer;
        this.board = new Board();
        this.diceManager = new DiceManager();
    }

    /**
     * Sunucuya veya istemciye ait client nesnesini bu sınıfa tanımlar.
     * @param client MultiClientClient nesnesi
     */
    public void setClient(MultiClientClient client) {
        this.client = client;
    }

    /**
     * Oyun tahtasını döndürür.
     * @return Oyun tahtası (Board)
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Zarların atılıp atılmadığını döndürür.
     * @return Zarlar atıldıysa true, atılmadıysa false
     */
    public boolean isDiceRolled() {
        return diceRolled;
    }

    /**
     * Sıradaki oyuncuyu döndürür.
     * @return Mevcut (hamle sırası olan) oyuncu
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Beyaz oyuncuyu döndürür.
     * @return Beyaz oyuncu
     */
    public Player getWhitePlayer() {
        return whitePlayer;
    }

    /**
     * Siyah oyuncuyu döndürür.
     * @return Siyah oyuncu
     */
    public Player getBlackPlayer() {
        return blackPlayer;
    }

    /**
     * Zarları yöneten DiceManager nesnesini döndürür.
     * @return DiceManager nesnesi
     */
    public DiceManager getDiceManager() {
        return diceManager;
    }

    /**
     * O an oynanabilecek kalan zar değerlerini (hamle değerlerini) döndürür.
     * @return Kalan oynanabilir zar değerleri listesi
     */
    public List<Integer> getRemainingMoves() {
        return new ArrayList<>(moveValues);
    }

    /**
     * Zar değerlerini elle ayarlar ve oynanabilir hamle listesini günceller.
     * Genellikle sunucudan gelen zar mesajlarında kullanılır.
     * @param die1 Birinci zar değeri
     * @param die2 İkinci zar değeri
     */
    public void setDiceValues(int die1, int die2) {
        diceManager.setDie1(die1);
        diceManager.setDie2(die2);
        moveValues.clear();
        if (die1 == die2) {
            // Çifte geldiğinde 4 hamle hakkı olur
            for (int i = 0; i < 4; i++) {
                moveValues.add(die1);
            }
        } else {
            moveValues.add(die1);
            moveValues.add(die2);
        }
        System.out.println("SERVER'DA GÜNCEL ZARLAR: " + moveValues);
    }

    /**
     * Zarları atar, yeni zar değerlerini belirler ve hamle listesini günceller.
     * Zarlar atıldıktan sonra client (istemci) varsa sunucuya sonucu gönderir.
     */
    public void rollDice() {
        if (diceRolled) {
            JOptionPane.showMessageDialog(null, "Hamlenizi tamamlamadan tekrar zar atamazsınız!");
            return;
        }
        diceManager.rollDice();
        diceRolled = true;
        moveValues.clear();

        int d1 = diceManager.getDie1();
        int d2 = diceManager.getDie2();

        if (d1 == d2) {
            // Çifte geldiğinde 4 hamle hakkı olur
            for (int i = 0; i < 4; i++) {
                moveValues.add(d1);
            }
        } else {
            moveValues.add(d1);
            moveValues.add(d2);
        }

        // Zar atma bilgisini sunucuya gönder
        if (client != null) {
            client.sendMessage("ROLL:" + d1 + "," + d2);
        }
    }

    /**
     * Oyuncu sırasını değiştirir (hamle hakkını diğer oyuncuya verir) ve zar atılabilir hale getirir.
     */
    public void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
        diceRolled = false;
    }

    /**
     * Belirtilen renkten oyuncunun bar’da (kırılmış taş) olup olmadığını kontrol eder.
     * @param color Oyuncunun rengi
     * @return Bar’da taş varsa true, yoksa false
     */
    public boolean hasBarChecker(Color color) {
        return board.getBarCount(color) > 0;
    }

    /**
     * Belirtilen oyuncunun oyunu kazanıp kazanmadığını kontrol eder.
     * Tüm taşlar dışarı çıkarılmışsa (borne off), oyuncu kazanır.
     * @param player Oyuncu
     * @return Oyuncu kazanmışsa true
     */
    public boolean hasWon(Player player) {
        return board.getBorneOff(player.getColor()) == 15;
    }

    /**
     * Oyuncu adından mevcut oyuncuyu belirler (sunucudan gelen bilgilere göre sıralamayı günceller).
     * @param playerName Oyuncu adı
     */
    public void setCurrentPlayerByName(String playerName) {
        if (whitePlayer.getName().equals(playerName)) {
            currentPlayer = whitePlayer;
        } else if (blackPlayer.getName().equals(playerName)) {
            currentPlayer = blackPlayer;
        }
    }

    /**
     * Bar'dan hamle yapılıp yapılamayacağını kontrol eder.
     * Bar'da taş varsa ve en az bir zar ile içeri girilebiliyorsa true döner.
     * @return Bar'dan çıkış mümkünse true
     */
    public boolean canEnterFromBar() {
        if (board.getBarCount(currentPlayer.getColor()) == 0) {
            return false;
        }
        for (int die : moveValues) {
            int targetIndex = (currentPlayer.getColor() == Color.WHITE) ? die - 1 : 24 - die;
            Point to = board.getPoint(targetIndex);
            if (to.isEmpty() || to.getColor() == currentPlayer.getColor() || (to.getColor() != currentPlayer.getColor() && to.getCount() == 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * UI'den değil, sunucudan gelen MOVE mesajlarını işlemek için kullanılır.
     * Sıra kontrolü yapmaz, doğrudan tahtayı günceller.
     * @param from Başlangıç noktası (indeks)
     * @param to   Bitiş noktası (indeks)
     */
    public void forceMoveChecker(int from, int to) {
        Point fromPoint = board.getPoint(from);
        Point toPoint = board.getPoint(to);

        if (fromPoint.isEmpty()) {
            return;
        }

        Color movingColor = fromPoint.getColor();

        // Rakip taşı kır
        if (!toPoint.isEmpty() && toPoint.getColor() != movingColor && toPoint.getCount() == 1) {
            board.addToBar(toPoint.getColor());
            toPoint.removeChecker();
        }

        // Rakip blok varsa iptal
        if (!toPoint.isEmpty() && toPoint.getColor() != movingColor && toPoint.getCount() > 1) {
            return;
        }

        // Hamleyi uygula
        try {
            toPoint.addChecker(movingColor);
            fromPoint.removeChecker();
            int distance = (movingColor == model.Color.WHITE) ? to - from : from - to;
            moveValues.remove((Integer) distance);

        } catch (IllegalStateException e) {
            System.err.println("Sunucudan gelen hamle uygulanamadı: " + e.getMessage());
        }
    }

    /**
     * Oyuncunun kendi taşını belirtilen yerden başka bir noktaya taşımaya çalışır.
     * Taş hamlesi geçerli ise işlemi gerçekleştirir ve server'a mesaj yollar.
     * @param from Başlangıç noktası (indeks)
     * @param to   Bitiş noktası (indeks)
     * @return Hamle geçerliyse true, değilse false
     */
    public boolean moveChecker(int from, int to) {
        Point fromPoint = board.getPoint(from);
        int distance = (currentPlayer.getColor() == Color.WHITE) ? to - from : from - to;

        if (fromPoint.isEmpty() || fromPoint.getColor() != currentPlayer.getColor() || distance <= 0 || !moveValues.contains(distance)) {
            return false;
        }

        Point toPoint = board.getPoint(to);

        // Rakip taşı kırma
        if (!toPoint.isEmpty() && toPoint.getColor() != currentPlayer.getColor() && toPoint.getCount() == 1) {
            board.addToBar(toPoint.getColor());
            toPoint.removeChecker();
        }

        // Rakip bloğu kontrolü
        if (!toPoint.isEmpty() && toPoint.getColor() != currentPlayer.getColor() && toPoint.getCount() > 1) {
            return false;
        }

        try {
            toPoint.addChecker(currentPlayer.getColor());
            fromPoint.removeChecker();
            moveValues.remove((Integer) distance);

            // Hareket bilgisini sunucuya gönder
            if (client != null) {
                client.sendMessage("MOVE:" + from + ":" + to);
            }

            if (moveValues.isEmpty() || !anyMovePossible()) {
                switchTurn();
            }
            return true;
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Geçersiz Hamle", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    /**
     * Bar'dan bir taşı tahtaya sokmayı dener. Başarılıysa sunucuya mesaj yollar.
     * @param toIndex Bar'dan hangi noktaya taş girecek
     * @return Hamle geçerliyse true
     */
    public boolean moveFromBar(int toIndex) {
        Color color = currentPlayer.getColor();
        for (int die : moveValues) {
            int target = (color == Color.WHITE) ? die - 1 : 24 - die;
            Point point = board.getPoint(toIndex);

            if (target == toIndex && (point.isEmpty() || point.getColor() == color || point.getCount() == 1)) {
                try {
                    point.addChecker(color);
                    board.removeFromBar(color);
                    moveValues.remove((Integer) die);

                    // Hareket bilgisini sunucuya gönder
                    if (client != null) {
                        client.sendMessage("MOVE_BAR:" + toIndex);
                    }

                    if (moveValues.isEmpty()) {
                        switchTurn();
                    }
                    return true;
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Geçersiz Hamle", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        return false;
    }

    /**
     * Taşların tamamı ev bölgesindeyse çıkış yapılabilir mi kontrol eder.
     * @param color Renk
     * @return Çıkış mümkünse true
     */
    public boolean canBearOff(Color color) {
        int start = (color == Color.WHITE) ? 18 : 0;
        int end = (color == Color.WHITE) ? 24 : 6;
        for (int i = 0; i < 24; i++) {
            if (i >= start && i < end) {
                continue;
            }
            Point p = board.getPoint(i);
            if (!p.isEmpty() && p.getColor() == color) {
                return false;
            }
        }
        return true;
    }

    /**
     * Herhangi bir taş için en az bir hamle mümkün mü kontrol eder.
     * Kalan hamlelerle hareket yapılabiliyorsa true döner.
     */
    private boolean anyMovePossible() {
        for (int from = 0; from < 24; from++) {
            Point point = board.getPoint(from);
            if (!point.isEmpty() && point.getColor() == currentPlayer.getColor()) {
                for (int move : moveValues) {
                    int to = (currentPlayer.getColor() == Color.WHITE) ? from + move : from - move;
                    if (to >= 0 && to < 24) {
                        Point toPoint = board.getPoint(to);
                        if (toPoint.isEmpty() || toPoint.getColor() == currentPlayer.getColor() || (toPoint.getColor() != currentPlayer.getColor() && toPoint.getCount() == 1)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Hiçbir taş için oynanabilir hamle yoksa true döner.
     * @return Hamle yapılamıyorsa true
     */
    public boolean hasNoAvailableMove() {
        for (int from = 0; from < 24; from++) {
            Point point = board.getPoint(from);
            if (!point.isEmpty() && point.getColor() == currentPlayer.getColor()) {
                for (int move : moveValues) {
                    int to = (currentPlayer.getColor() == Color.WHITE) ? from + move : from - move;
                    if (to >= 0 && to < 24) {
                        Point toPoint = board.getPoint(to);
                        if (toPoint.isEmpty() || toPoint.getColor() == currentPlayer.getColor() || (toPoint.getColor() != currentPlayer.getColor() && toPoint.getCount() == 1)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Bar'dan giriş yapılabilecek noktaların indekslerini döndürür.
     * @param color Hangi oyuncu için bakılacak
     * @return Giriş yapılabilecek indekslerin listesi
     */
    public List<Integer> getBarEntryTargets(Color color) {
        List<Integer> targets = new ArrayList<>();
        for (int move : moveValues) {
            int targetIndex = (color == Color.WHITE) ? move - 1 : 24 - move;
            Point point = board.getPoint(targetIndex);
            if (point.isEmpty() || point.getColor() == color || (point.getColor() != color && point.getCount() == 1)) {
                targets.add(targetIndex);
            }
        }
        return targets;
    }

    /**
     * Sadece doğrulama amacıyla hamle yapılabilir mi kontrol eder (tahtayı değiştirmez).
     * @param from Başlangıç noktası
     * @param to   Bitiş noktası
     * @return Hamle geçerli ise true
     */
    public boolean moveCheckerTestOnly(int from, int to) {
        Point fromPoint = board.getPoint(from);
        int distance = (currentPlayer.getColor() == Color.WHITE) ? to - from : from - to;

        // Hamle şartlarını kontrol et (oyuncunun taşı mı, zar uygun mu, blok var mı)
        if (fromPoint.isEmpty() || fromPoint.getColor() != currentPlayer.getColor() || distance <= 0 || !moveValues.contains(distance)) {
            return false;
        }

        Point toPoint = board.getPoint(to);
        if (!toPoint.isEmpty() && toPoint.getColor() != currentPlayer.getColor() && toPoint.getCount() > 1) {
            return false;
        }
        return true;
    }

    /**
     * Sıradaki oyuncuyu doğrudan ayarlamak için kullanılır.
     * @param player Yeni currentPlayer
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    /**
     * Bar'dan belli bir noktaya çıkış yapılıp yapılamayacağını kontrol eder.
     * @param targetIndex Hedef nokta
     * @return Çıkış yapılabiliyorsa true
     */
    public boolean canEnterFromBarTo(int targetIndex) {
        Color color = currentPlayer.getColor();
        for (int die : moveValues) {
            int hedef = (color == Color.WHITE) ? die - 1 : 24 - die;
            if (hedef == targetIndex) {
                Point point = board.getPoint(targetIndex);
                if (point.isEmpty() || point.getColor() == color || point.getCount() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sunucu için: Bar’dan taş çıkarma işlemini uygular.
     * @param toIndex Hangi noktaya taş girecek
     */
    public void moveFromBarServer(int toIndex) {
        Color color = currentPlayer.getColor();
        for (int die : moveValues) {
            int hedef = (color == Color.WHITE) ? die - 1 : 24 - die;
            if (hedef == toIndex) {
                Point point = board.getPoint(toIndex);
                System.out.println("BAR MOVE - Önce: Bar=" + board.getBarCount(color) + " -> [" + toIndex + "] " + point.getCount() + " taş");
                if (point.isEmpty() || point.getColor() == color || point.getCount() == 1) {
                    // Rakip taşı kır
                    if (!point.isEmpty() && point.getColor() != color && point.getCount() == 1) {
                        board.addToBar(point.getColor());
                        point.removeChecker();
                    }
                    point.addChecker(color);
                    board.removeFromBar(color);
                    moveValues.remove((Integer) die);
                    System.out.println("BAR MOVE - Sonra: Bar=" + board.getBarCount(color) + " -> [" + toIndex + "] " + point.getCount() + " taş");
                    return;
                }
            }
        }
    }

    /**
     * Oyuncu taşını tahtadan çıkarmaya (bear off) çalışır.
     * Hamle başarılı olursa sıra değiştirilir ve sunucuya bildirilir.
     * @param fromIndex Hangi noktadaki taş çıkacak
     * @return Taş başarıyla çıkarsa true
     */
    public boolean tryBearOff(int fromIndex) {
        Color color = currentPlayer.getColor();
        if (!canBearOff(color)) {
            return false;
        }

        int bearingValue = (color == Color.WHITE) ? 23 - fromIndex + 1 : fromIndex + 1;

        // Zar değerlerinden biri hedef değerle eşleşiyor mu?
        for (int die : moveValues) {
            // Eğer taş doğrudan çıkarılabiliyorsa
            if (die == bearingValue) {
                Point fromPoint = board.getPoint(fromIndex);
                if (!fromPoint.isEmpty() && fromPoint.getColor() == color) {
                    fromPoint.removeChecker();
                    board.bearOff(color);
                    moveValues.remove((Integer) die);

                    // Hareket bilgisini sunucuya gönder
                    if (client != null) {
                        client.sendMessage("BEAR_OFF:" + fromIndex);
                    }

                    // Hamle tamamlandıktan sonra sıra değiştirme
                    if (moveValues.isEmpty()) {
                        switchTurn();
                    }
                    return true;
                }
            }

            // Eğer taş çıkışı yapamıyorsa ama daha küçük bir taş varsa
            if (die > bearingValue) {
                boolean hasCheckerBehind = false;
                int start = (color == Color.WHITE) ? 18 : 0;
                int end = (color == Color.WHITE) ? 24 : 6;

                // Daha küçük pozisyonlarda aynı renkten taş var mı kontrol et
                for (int i = start; i < end; i++) {
                    if (i != fromIndex) {
                        Point p = board.getPoint(i);
                        if (!p.isEmpty() && p.getColor() == color) {
                            hasCheckerBehind = true;
                            break;
                        }
                    }
                }

                // Eğer arkada başka taş yoksa çıkar
                if (!hasCheckerBehind) {
                    Point fromPoint = board.getPoint(fromIndex);
                    if (!fromPoint.isEmpty() && fromPoint.getColor() == color) {
                        fromPoint.removeChecker();
                        board.bearOff(color);
                        moveValues.remove((Integer) die);

                        // Hareket bilgisini sunucuya gönder
                        if (client != null) {
                            client.sendMessage("BEAR_OFF:" + fromIndex);
                        }

                        // Hamle tamamlandıktan sonra sıra değiştirme
                        if (moveValues.isEmpty()) {
                            switchTurn();
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
