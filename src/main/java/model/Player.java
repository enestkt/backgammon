package model;

public class Player {
    private String name;
    private Color color;
    private int borneOffCount;
    private int barCount;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.borneOffCount = 0;
        this.barCount = 0;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getBorneOffCount() {
        return borneOffCount;
    }

    public void incrementBorneOff() {
        borneOffCount++;
    }

    public boolean hasWon() {
        return borneOffCount == 15;
    }

    public int getBarCount() {
        return barCount;
    }

    public void incrementBar() {
        barCount++;
    }

    public void decrementBar() {
        if (barCount > 0) barCount--;
    }
}
