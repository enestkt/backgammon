package ui;

import logic.GameManager;
import model.Color;
import network.MultiClientClient;

import javax.swing.*;
import java.awt.*;
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
    private boolean isMyTurn = false;

    public void setTurn(boolean turn) {
        this.isMyTurn = turn;
        rollButton.setEnabled(isMyTurn);
        passButton.setEnabled(isMyTurn);
    }

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
        passButton.setEnabled(false); // Başlangıçta pasif
        passButton.addActionListener(e -> {
            if (!isMyTurn) {
                return;
            }
            if (gameManager.hasNoAvailableMove()) {
                JOptionPane.showMessageDialog(this, "Hareket yapacak taşınız yok. Sıra diğer oyuncuya geçti.");
                // YALNIZCA SUNUCUYA MESAJ GÖNDER
                client.sendMessage("SWITCH_TURN:");
                // Local olarak gameManager.switchTurn(); veya updateInfo() YOK!
            } else {
                JOptionPane.showMessageDialog(this, "Hareket yapabileceğiniz taşlar var!");
            }
        });

        buttonPanel.add(passButton);

        // "Zar At" Butonu
        rollButton = new JButton("🎲 Zar At");
        rollButton.setFont(new Font("Arial", Font.BOLD, 14));
        rollButton.setPreferredSize(new Dimension(280, 40));
        rollButton.setEnabled(false); // Başlangıçta pasif
        rollButton.addActionListener(e -> {
            if (!isMyTurn) {
                return;
            }
            if (gameManager.isDiceRolled()) {
                JOptionPane.showMessageDialog(this, "Zaten zar attınız, hamlenizi yapın!");
            } else {
                if (gameManager.getRemainingMoves().size() == 0) {
                    // *** LOCAL rollDice() YOK! ***
                    // Zar değerini burada oluşturabilirsin ya da server tarafında random ata
                    int die1 = (int) (Math.random() * 6) + 1;
                    int die2 = (int) (Math.random() * 6) + 1;
                    client.sendRoll(die1, die2);
                    // Local olarak updateInfo() veya başka bir şey yok!
                } else {
                    System.out.println("karşı rakip tüm hamlesini daha yapmadı");
                }
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
            String playerName = gameManager.getCurrentPlayer().getName();
            if (!message.isEmpty()) {
                client.sendChat(message);
                chatArea.append(playerName + ": " + message + "\n");
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