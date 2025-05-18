package logic;

import model.Die;

/**
 * DiceManager: 2 zarın değerlerini tutar ve yönetir.
 */
public class DiceManager {

    private Die die1;
    private Die die2;

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

    /**
     * Zar değerlerini ayarlar.
     */
    public void setDiceValues(int value1, int value2) {
        die1.setValue(value1);
        die2.setValue(value2);
    }

    /**
     * İlk zarın değerini döndürür.
     */
    public int getDie1() {
        return die1.getValue();
    }

    /**
     * İkinci zarın değerini döndürür.
     */
    public int getDie2() {
        return die2.getValue();
    }

    /**
     * İlk zarın değerini ayarlar.
     */
    public void setDie1(int value) {
        die1.setValue(value);
    }

    /**
     * İkinci zarın değerini ayarlar.
     */
    public void setDie2(int value) {
        die2.setValue(value);
    }
}
