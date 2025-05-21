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

public class GameManager {

    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;
    private DiceManager diceManager;
    private List<Integer> moveValues = new ArrayList<>();
    private boolean diceRolled = false;
    private MultiClientClient client;

    public GameManager(String whiteName, String blackName) {
        this.whitePlayer = new Player(whiteName, Color.WHITE);
        this.blackPlayer = new Player(blackName, Color.BLACK);
        this.currentPlayer = whitePlayer;
        this.board = new Board();
        this.diceManager = new DiceManager();
    }

    public void setClient(MultiClientClient client) {
        this.client = client;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isDiceRolled() {
        return diceRolled;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public DiceManager getDiceManager() {
        return diceManager;
    }

    public List<Integer> getRemainingMoves() {
        return new ArrayList<>(moveValues);
    }

    public void setDiceValues(int die1, int die2) {
        diceManager.setDie1(die1);
        diceManager.setDie2(die2);
    }

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

    public void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
        diceRolled = false;
    }

    public boolean hasBarChecker(Color color) {
        return board.getBarCount(color) > 0;
    }

    public boolean hasWon(Player player) {
        return board.getBorneOff(player.getColor()) == 15;
    }

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
    // UI'den değil, sunucudan gelen MOVE mesajlarını işlemek için kullanılır.
// Sıra kontrolü yapmaz, doğrudan tahtayı günceller.

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
            switchTurn(); // sırayı değiştir
        } catch (IllegalStateException e) {
            System.err.println("Sunucudan gelen hamle uygulanamadı: " + e.getMessage());
        }
    }

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