package model;

import java.util.HashMap;
import java.util.Map;

public class Board {

    private Point[] points;
    private Map<Color, Integer> bar;
    private Map<Color, Integer> borneOff;

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

    private void placeCheckers(int index, Color color, int count) {
        for (int i = 0; i < count; i++) {
            points[index].addChecker(color);
        }
    }

    public Point getPoint(int index) {
        return points[index];
    }

    public int getBarCount(Color color) {
        return bar.get(color);
    }

    public void addToBar(Color color) {
        bar.put(color, bar.get(color) + 1);
    }

    public void removeFromBar(Color color) {
        bar.put(color, bar.get(color) - 1);
    }

    public int getBorneOff(Color color) {
        return borneOff.get(color);
    }

    public void bearOff(Color color) {
        borneOff.put(color, borneOff.get(color) + 1);
    }

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
