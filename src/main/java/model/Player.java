package model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private Color color;
    private int borneOffCount;
    private int barCount;

    // === EKLENEN KISIM ===
    private List<Integer> moveValues = new ArrayList<>();

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

    // === EKLENEN KISIM ===
    public List<Integer> getMoveValues() {
        return moveValues;
    }

    public void setMoveValues(List<Integer> values) {
        moveValues.clear();
        moveValues.addAll(values);
    }
}
