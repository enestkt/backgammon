package logic;

import model.*;

public class MoveValidator {

    private Board board;

    public MoveValidator(Board board) {
        this.board = board;
    }

    public boolean isValidMove(Move move, Player player) {
        Color color = player.getColor();

        int from = move.getFrom();
        int to = move.getTo();

        // 1. Bar kontrolü
        if (board.getBarCount(color) > 0 && !move.isFromBar()) {
            return false;
        }

        // 2. to aralığı kontrolü
        if (to < 0 || to > 23) {
            return false;
        }

        Point destination = board.getPoint(to);

        // 3. Gidilecek yer bloklu mu?
        if (destination.isBlockedFor(color)) {
            return false;
        }

        // 4. Bar'dan geliyorsa from = -1
        if (move.isFromBar()) {
            return !destination.isBlockedFor(color);
        }

        // 5. Normal taş oynama: from geçerli mi?
        if (from < 0 || from > 23) {
            return false;
        }

        Point origin = board.getPoint(from);
        if (!origin.isOwnedBy(color)) {
            return false;
        }

        return true;
    }

    public boolean isBearOffPossible(Player player, int fromIndex, int diceValue) {
        Color color = player.getColor();
        if (!areAllCheckersInHomeBoard(color)) return false;

        if (color == Color.WHITE) {
            return fromIndex + diceValue >= 24;
        } else {
            return fromIndex - diceValue < 0;
        }
    }

    private boolean areAllCheckersInHomeBoard(Color color) {
        int start = (color == Color.WHITE) ? 18 : 0;
        int end = (color == Color.WHITE) ? 23 : 5;

        for (int i = 0; i < 24; i++) {
            Point p = board.getPoint(i);
            if (p.getColor() == color && (i < start || i > end)) {
                return false;
            }
        }
        return true;
    }
}
