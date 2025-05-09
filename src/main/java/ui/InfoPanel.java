package ui;

import logic.GameManager;
import model.Color;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import network.Client;

/**
 * InfoPanel: Oyuncu bilgileri, zarlar, bar ve "Zar At" butonunu içeren panel.
 */
public class InfoPanel extends JPanel {

    private GameManager gameManager;
    private GamePanel gamePanel;

    private JLabel playerLabel;
    private JLabel diceLabel;
    private JLabel remainingLabel;
    private JLabel barWhiteLabel;
    private JLabel barBlackLabel;
    private JButton rollButton;

    public InfoPanel(GameManager gameManager, GamePanel gamePanel) {
        this.gameManager = gameManager;
        this.gamePanel = gamePanel;

        setPreferredSize(new Dimension(200, 600));
        setBackground(new java.awt.Color(230, 230, 250)); // Lavanta renginde arka plan
        setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY, 2));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        playerLabel = new JLabel();
        diceLabel = new JLabel();
        remainingLabel = new JLabel();
        barWhiteLabel = new JLabel();
        barBlackLabel = new JLabel();

        playerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        diceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        remainingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        barWhiteLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        barBlackLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        add(Box.createVerticalStrut(10));
        add(playerLabel);
        add(Box.createVerticalStrut(10));
        add(diceLabel);
        add(Box.createVerticalStrut(10));
        add(remainingLabel);
        add(Box.createVerticalStrut(10));
        add(barWhiteLabel);
        add(Box.createVerticalStrut(5));
        add(barBlackLabel);
        add(Box.createVerticalStrut(20));

        rollButton = new JButton("🎲 Zar At");
        // Zar At butonunun altına ekle
        JButton sendBoardButton = new JButton("Tahtayı Gönder");
        sendBoardButton.addActionListener(e -> {
            try {
                Client client = new Client("127.0.0.1", 5000); // 5000 → server'daki port ile aynı olmalı
                // ← Sunucu IP’sini burada değiştir
                client.sendBoard(gameManager.getBoard());
                JOptionPane.showMessageDialog(this, "Tahta başarıyla gönderildi.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gönderme başarısız: " + ex.getMessage());
            }
        });
        add(sendBoardButton);

        rollButton.addActionListener(e -> {
            gameManager.rollDice();
            gamePanel.repaint();
            updateInfo();
        });
        add(rollButton);

        updateInfo();
    }

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
