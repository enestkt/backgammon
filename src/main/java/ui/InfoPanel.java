package ui;

import logic.GameManager;
import model.Color;
import network.MultiClientClient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * InfoPanel, oyuncu adı, zarlar, kalan hamleler, bar bilgisi ve kontrol butonlarını gösteren Swing panelidir.
 */
public class InfoPanel extends JPanel {

    // Oyun mantığı
    private GameManager gameManager;

    // Ağ üzerinden sunucuya mesaj göndermek için istemci
    private MultiClientClient client;

    // GUI bileşenleri
    private JLabel playerLabel;
    private JLabel diceLabel;
    private JLabel remainingLabel;
    private JLabel barWhiteLabel;
    private JLabel barBlackLabel;
    private JButton rollButton;
    private JButton passButton;
    private boolean isMyTurn = false;

    /**
     * Oyuncunun sırası geldiğinde butonların aktifliğini ayarlar.
     * @param turn Sıra sende mi
     */
    public void setTurn(boolean turn) {
        this.isMyTurn = turn;
        rollButton.setEnabled(isMyTurn);
        passButton.setEnabled(isMyTurn);
    }

    /**
     * InfoPanel yapıcı metodu.
     * @param gameManager Oyun mantığı
     * @param client Ağ üzerinden mesaj göndermek için istemci
     */
    public InfoPanel(GameManager gameManager, MultiClientClient client) {
        this.gameManager = gameManager;
        this.client = client;

        setPreferredSize(new Dimension(300, 600));
        setBackground(new java.awt.Color(230, 230, 250));
        setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY, 2));
        setLayout(new BorderLayout());

        playerLabel = new JLabel("Oyuncu Bilgisi");
        diceLabel = new JLabel("Zarlar: -");
        remainingLabel = new JLabel("Kalan Zarlar: -");
        barWhiteLabel = new JLabel("Bar (WHITE): 0");
        barBlackLabel = new JLabel("Bar (BLACK): 0");

        Font infoFont = new Font("Arial", Font.BOLD, 16);
        playerLabel.setFont(infoFont);
        diceLabel.setFont(infoFont);
        remainingLabel.setFont(infoFont);
        barWhiteLabel.setFont(infoFont);
        barBlackLabel.setFont(infoFont);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(6, 1, 5, 5));
        infoPanel.add(playerLabel);
        infoPanel.add(diceLabel);
        infoPanel.add(remainingLabel);
        infoPanel.add(barWhiteLabel);
        infoPanel.add(barBlackLabel);

        // Buton Paneli
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        // "Hamle Yapamıyorum" Butonu
        passButton = new JButton("Hamle Yapamıyorum");
        passButton.setFont(new Font("Arial", Font.PLAIN, 12));
        passButton.setPreferredSize(new Dimension(280, 30));
        passButton.setEnabled(false);
        passButton.addActionListener(e -> {
            if (!isMyTurn) {
                return;
            }
            if (gameManager.hasNoAvailableMove()) {
                JOptionPane.showMessageDialog(this, "Hareket yapacak taşınız yok. Sıra diğer oyuncuya geçti.");
                client.sendMessage("SWITCH_TURN:");
            } else {
                JOptionPane.showMessageDialog(this, "Hareket yapabileceğiniz taşlar var!");
            }
        });
        buttonPanel.add(passButton);

        // "Zar At" Butonu
        rollButton = new JButton("🎲 Zar At");
        rollButton.setFont(new Font("Arial", Font.BOLD, 14));
        rollButton.setPreferredSize(new Dimension(280, 40));
        rollButton.setEnabled(false);
        rollButton.addActionListener(e -> {
            if (!isMyTurn) {
                return;
            }
            if (gameManager.isDiceRolled()) {
                JOptionPane.showMessageDialog(this, "Zaten zar attınız, hamlenizi yapın!");
            } else {
                if (gameManager.getRemainingMoves().size() == 0) {
                    client.sendMessage("ROLL:");
                } else {
                    System.out.println("karşı rakip tüm hamlesini daha yapmadı");
                }
            }
        });
        buttonPanel.add(rollButton);
        infoPanel.add(buttonPanel);

        add(infoPanel, BorderLayout.NORTH);
    }

    /**
     * Oyuncu, zar ve bar bilgilerini günceller (GUI üstündeki yazılar yenilenir).
     */
    public void updateInfo() {
        playerLabel.setText("Sıra: " + gameManager.getCurrentPlayer().getName()
                + " (" + gameManager.getCurrentPlayer().getColor().toString().toUpperCase() + ")");

        int d1 = gameManager.getDiceManager().getDie1();
        int d2 = gameManager.getDiceManager().getDie2();
        diceLabel.setText("Zarlar: " + d1 + " - " + d2);

        List<Integer> moves = gameManager.getRemainingMoves();
        remainingLabel.setText("Kalan Zarlar: " + (moves.isEmpty() ? "-" : moves.toString()));

        barWhiteLabel.setText("Bar (WHITE): " + gameManager.getBoard().getBarCount(Color.WHITE));
        barBlackLabel.setText("Bar (BLACK): " + gameManager.getBoard().getBarCount(Color.BLACK));
    }
}
