package ui;

import logic.GameManager;
import model.Color;
import network.MultiClientClient;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class InfoPanel extends JPanel {

    private GameManager gameManager;
    private MultiClientClient client;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendButton;
    private JLabel playerLabel;
    private JLabel diceLabel;
    private JLabel remainingLabel;
    private JLabel barWhiteLabel;
    private JLabel barBlackLabel;
    private JButton rollButton;
    private JButton passButton;

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

        playerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        diceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        remainingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        barWhiteLabel.setFont(new Font("Arial", Font.BOLD, 16));
        barBlackLabel.setFont(new Font("Arial", Font.BOLD, 16));

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
        passButton.addActionListener(e -> handlePassTurn());
        buttonPanel.add(passButton);

        // "Zar At" Butonu
        rollButton = new JButton("🎲 Zar At");
        rollButton.setFont(new Font("Arial", Font.BOLD, 14));
        rollButton.setPreferredSize(new Dimension(280, 40));
        rollButton.addActionListener(e -> {
            if (gameManager.isDiceRolled()) {
                JOptionPane.showMessageDialog(this, "Zaten zar attınız, hamlenizi yapın!");
            } else {
                gameManager.rollDice();
                updateInfo();
            }
        });
        buttonPanel.add(rollButton);

        infoPanel.add(buttonPanel);

        // Sohbet Alanı
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(280, 150));

        chatInput = new JTextField();
        sendButton = new JButton("Gönder");

        sendButton.addActionListener(e -> {
            String message = chatInput.getText();
            if (!message.isEmpty()) {
                client.sendMessage(message);
                chatArea.append("Ben: " + message + "\n");
                chatInput.setText("");
            }
        });

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatInput, BorderLayout.CENTER);
        chatPanel.add(sendButton, BorderLayout.EAST);

        add(infoPanel, BorderLayout.NORTH);
        add(chatScroll, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.SOUTH);
    }

    public void updateChat(String message) {
        chatArea.append(message + "\n");
    }

    public void updateInfo() {
        playerLabel.setText("Sıra: " + gameManager.getCurrentPlayer().getName() + 
            " (" + gameManager.getCurrentPlayer().getColor().toString().toUpperCase() + ")");

        int d1 = gameManager.getDiceManager().getDie1();
        int d2 = gameManager.getDiceManager().getDie2();
        diceLabel.setText("Zarlar: " + d1 + " - " + d2);

        List<Integer> moves = gameManager.getRemainingMoves();
        remainingLabel.setText("Kalan Zarlar: " + (moves.isEmpty() ? "-" : moves.toString()));

        barWhiteLabel.setText("Bar (WHITE): " + gameManager.getBoard().getBarCount(Color.WHITE));
        barBlackLabel.setText("Bar (BLACK): " + gameManager.getBoard().getBarCount(Color.BLACK));
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    private void handlePassTurn() {
        if (gameManager.hasNoAvailableMove()) {
            JOptionPane.showMessageDialog(this, "Hareket yapacak taşınız yok. Sıra diğer oyuncuya geçti.");
            gameManager.switchTurn();
            updateInfo();
        } else {
            JOptionPane.showMessageDialog(this, "Hareket yapabileceğiniz taşlar var!");
        }
    }
}
