package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import model.Board;
import model.Color;
import model.Player;
import model.Point;

public class GameManager {

    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;
    private DiceManager diceManager;
    private List<Integer> moveValues = new ArrayList<>();

    public GameManager(String whiteName, String blackName) {
        this.whitePlayer = new Player(whiteName, Color.WHITE);
        this.blackPlayer = new Player(blackName, Color.BLACK);
        this.currentPlayer = whitePlayer;
        this.board = new Board();
        this.diceManager = new DiceManager();
    }

    public Board getBoard() {
        return board;
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

    public void rollDice() {
        diceManager.rollDice();
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
    }

    public boolean hasBarChecker(Color color) {
        return board.getBarCount(color) > 0;
    }

    public void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
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
            if (targetIndex < 0 || targetIndex >= 24) {
                continue;
            }
            Point to = board.getPoint(targetIndex);
            if (to.isEmpty() || to.getColor() == currentPlayer.getColor() || (to.getColor() != currentPlayer.getColor() && to.getCount() == 1)) {
                return true;
            }
        }
        return false;
    }

    public boolean tryBearOff(int fromIndex) {
        Color color = currentPlayer.getColor();
        if (!canBearOff(color)) {
            return false;
        }

        int bearingValue = (color == Color.WHITE) ? 23 - fromIndex + 1 : fromIndex + 1;

        for (int die : moveValues) {
            if (die == bearingValue) {
                Point fromPoint = board.getPoint(fromIndex);
                if (!fromPoint.isEmpty() && fromPoint.getColor() == color) {
                    fromPoint.removeChecker();
                    board.bearOff(color);
                    moveValues.remove((Integer) die);
                    if (moveValues.isEmpty()) {
                        switchTurn();
                    }
                    return true;
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

    public List<Integer> getBarEntryTargets(Color color) {
        List<Integer> targets = new ArrayList<>();
        for (int move : moveValues) {
            int targetIndex = (color == Color.WHITE) ? move - 1 : 24 - move;
            if (targetIndex >= 0 && targetIndex < 24) {
                Point point = board.getPoint(targetIndex);
                if (point.isEmpty()
                        || point.getColor() == color
                        || (point.getColor() != color && point.getCount() == 1)) {
                    targets.add(targetIndex);
                }
            }
        }
        return targets;
    }

    public boolean moveChecker(int from, int to) {
        Point fromPoint = board.getPoint(from);
        if (fromPoint.isEmpty() || fromPoint.getColor() != currentPlayer.getColor()) {
            return false;
        }
        int distance = (currentPlayer.getColor() == Color.WHITE) ? to - from : from - to;

        if ((currentPlayer.getColor() == Color.WHITE && to >= 24) || (currentPlayer.getColor() == Color.BLACK && to < 0)) {
            if (tryBearOff(from)) {
                if (hasWon(currentPlayer)) {
                    JOptionPane.showMessageDialog(null, currentPlayer.getName() + " kazandı!");
                }
                return true;
            }
            return false;
        }

        if (distance <= 0 || !moveValues.contains(distance)) {
            return false;
        }
        Point toPoint = board.getPoint(to);

        if (!toPoint.isEmpty() && toPoint.getColor() != currentPlayer.getColor() && toPoint.getCount() == 1) {
            board.addToBar(toPoint.getColor());
            toPoint.removeChecker();
        }

        if (!toPoint.isEmpty() && toPoint.getColor() != currentPlayer.getColor() && toPoint.getCount() > 1) {
            return false;
        }

        try {
            toPoint.addChecker(currentPlayer.getColor());
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Geçersiz Hamle", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        fromPoint.removeChecker();
        moveValues.remove((Integer) distance);

        if (moveValues.isEmpty() || !anyMovePossible()) {
            switchTurn();
        }

        return true;
    }

    public boolean moveFromBar(int toIndex) {
        Color color = currentPlayer.getColor();
        int fromIndex = (color == Color.WHITE) ? -1 : -2;
        int expectedIndex = (color == Color.WHITE) ? toIndex : 23 - toIndex;

        for (int die : moveValues) {
            int target = (color == Color.WHITE) ? die - 1 : 24 - die;
            if (target == toIndex) {
                Point point = board.getPoint(toIndex);

                if (point.isEmpty() || point.getColor() == color || point.getCount() == 1) {
                    if (!point.isEmpty() && point.getColor() != color && point.getCount() == 1) {
                        board.addToBar(point.getColor());
                        point.removeChecker();
                    }

                    try {
                        point.addChecker(color);
                    } catch (IllegalStateException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Geçersiz Hamle", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    board.removeFromBar(color);
                    moveValues.remove((Integer) die);
                    if (moveValues.isEmpty()) {
                        switchTurn();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean anyMovePossible() {
        Color playerColor = currentPlayer.getColor();
        if (board.getBarCount(playerColor) > 0) {
            return canEnterFromBar();
        }

        for (int from = 0; from < 24; from++) {
            Point fromPoint = board.getPoint(from);
            if (!fromPoint.isEmpty() && fromPoint.getColor() == playerColor) {
                for (int move : moveValues) {
                    int to = (playerColor == Color.WHITE) ? from + move : from - move;
                    if (to >= 0 && to < 24) {
                        Point toPoint = board.getPoint(to);
                        if (toPoint.isEmpty() || toPoint.getColor() == playerColor || (toPoint.getColor() != playerColor && toPoint.getCount() == 1)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
