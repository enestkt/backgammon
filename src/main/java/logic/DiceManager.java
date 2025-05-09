package logic;

import model.Die;

/**
 * DiceManager: 2 zarın değerlerini tutar ve yönetir.
 */
public class DiceManager {
    private final Die die1;
    private final Die die2;

    public DiceManager() {
        die1 = new Die();
        die2 = new Die();
    }

    /**
     * Zarları atar ve her iki zar için yeni değer üretir.
     */
    public void rollDice() {
        die1.roll();
        die2.roll();
    }

    public int getDie1() {
        return die1.getValue();
    }

    public int getDie2() {
        return die2.getValue();
    }
}
