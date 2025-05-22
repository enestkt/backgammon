package model;

import java.io.Serializable;

public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int index;
    private Color color;
    private int count;

    public Point(int index) {
        
        this.index = index;
        this.color = null;
        this.count = 0;
    }

    public int getIndex() {
        return index;
    }
    

    public Color getColor() {
        return color;
    }

    public int getCount() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean isOwnedBy(Color c) {
        return !isEmpty() && color == c;
    }

    public boolean isBlockedFor(Color c) {
        return !isEmpty() && color != c && count >= 2;
    }

    public void addChecker(Color color) {
        if (!isEmpty() && this.color != color && count > 1) {
            throw new IllegalStateException("Cannot place checker on opponent's point with more than 1 checker.");
        }

        if (count >= 6) {
            throw new IllegalStateException("Bu noktada maksimum 6 taş olabilir.");
        }

        if (!isEmpty() && this.color != color && count == 1) {
            removeChecker();
            System.out.println("Rakip taşı kırdın!");
        }

        this.color = color;
        count++;
    }

    public void removeChecker() {
        if (count > 0) {
            count--;
            if (count == 0) {
                color = null;
            }
        } else {
            throw new IllegalStateException("No checkers to remove.");
        }
    }
    // Kaç taş ve rengine göre hızlıca ayarla (pozitif: WHITE, negatif: BLACK, 0: boş)
public void setCountAndColor(int value) {
    if (value == 0) {
        this.setCount(0);
        this.setColor(null); // veya boş renk
    } else if (value > 0) {
        this.setCount(value);
        this.setColor(Color.WHITE);
    } else {
        this.setCount(-value);
        this.setColor(Color.BLACK);
    }
}

    private void setCount(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void setColor(Object object) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
