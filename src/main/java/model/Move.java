package model;

public class Move {
    private int from;
    private int to;
    private Color playerColor;
    private boolean isHit;

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

    public boolean isFromBar() {
        return from == -1;
    }

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
