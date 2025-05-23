package logic;

import model.Die;

/**
 * DiceManager sınıfı, tavla oyununda kullanılan iki zarın değerlerini tutar ve yönetir.
 * Zar atma, zar değerlerini ayarlama ve okuma işlemlerini gerçekleştirir.
 */
public class DiceManager {

    // Birinci zar nesnesi
    private Die die1;

    // İkinci zar nesnesi
    private Die die2;

    /**
     * DiceManager yapıcı metodu.
     * İki zar nesnesini oluşturur ve başlatır.
     */
    public DiceManager() {
        die1 = new Die();
        die2 = new Die();
    }

    /**
     * Zarları atar ve her iki zar için rastgele yeni bir değer üretir.
     * Her iki zar da bağımsız olarak 'roll' metodu ile atılır.
     */
    public void rollDice() {
        die1.roll();
        die2.roll();
    }

    /**
     * Zarların değerlerini elle ayarlamak için kullanılır.
     * Genellikle oyun başında veya özel durumlarda kullanılır.
     *
     * @param value1 Birinci zarın atanacak değeri
     * @param value2 İkinci zarın atanacak değeri
     */
    public void setDiceValues(int value1, int value2) {
        die1.setValue(value1);
        die2.setValue(value2);
    }

    /**
     * Birinci zarın mevcut değerini döndürür.
     *
     * @return Birinci zarın (die1) güncel değeri
     */
    public int getDie1() {
        return die1.getValue();
    }

    /**
     * İkinci zarın mevcut değerini döndürür.
     *
     * @return İkinci zarın (die2) güncel değeri
     */
    public int getDie2() {
        return die2.getValue();
    }

    /**
     * Birinci zarın değerini manuel olarak ayarlar.
     * @param value Birinci zarın atanacak yeni değeri
     */
    public void setDie1(int value) {
        die1.setValue(value);
    }

    /**
     * İkinci zarın değerini manuel olarak ayarlar.
     * @param value İkinci zarın atanacak yeni değeri
     */
    public void setDie2(int value) {
        die2.setValue(value);
    }
}
