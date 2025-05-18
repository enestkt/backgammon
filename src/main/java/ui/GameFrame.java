package ui;

import logic.GameManager;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.MultiClientClient;

public class GameFrame extends JFrame {

    private final GameManager gameManager;
    private final GamePanel gamePanel;
    private final InfoPanel infoPanel;
    private final MultiClientClient client;

    public GameFrame(GameManager gameManager, MultiClientClient client) throws IOException {
        this.gameManager = gameManager;
        this.client = client;

        setTitle("Tavla - Oyun");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        gamePanel = new GamePanel(gameManager);
        infoPanel = new InfoPanel(gameManager, client);

        gamePanel.setInfoPanel(infoPanel);
        add(infoPanel, BorderLayout.WEST);
        add(gamePanel, BorderLayout.CENTER);

        setVisible(true);
        startListeningForUpdates();
    }

    private void startListeningForUpdates() {
        new Thread(() -> {
            while (true) {
                String message = client.receiveMessage();
                if (message == null) {
                    System.err.println("Bağlantı kesildi, dinleme sonlandırıldı.");
                    break;  // Bağlantı koptuğunda döngüden çık
                }
                SwingUtilities.invokeLater(() -> processMessage(message));
            }
        }).start();
    }

    private void processMessage(String message) {
        try {
            if (message.startsWith("ROLL:")) {
                String[] parts = message.split(":");
                int die1 = Integer.parseInt(parts[2]);
                int die2 = Integer.parseInt(parts[3]);
                gameManager.setDiceValues(die1, die2);
                infoPanel.updateInfo();
                gamePanel.repaint();
            } else if (message.startsWith("MOVE:")) {
                String[] parts = message.split(":");
                int from = Integer.parseInt(parts[2]);
                int to = Integer.parseInt(parts[3]);
                gameManager.moveChecker(from, to);
                infoPanel.updateInfo();
                gamePanel.repaint();
            } else if (message.startsWith("CHAT:")) {
                infoPanel.updateChat(message.substring(5));
            }
        } catch (Exception e) {
            System.err.println("Mesaj işleme hatası: " + e.getMessage());
        }
    }

    public void sendMove(int from, int to) {
        client.sendMove(from, to);
    }

    public void sendRollDice(int die1, int die2) {
        client.sendRoll(die1, die2);
    }

    public void sendChatMessage(String chatMessage) {
        client.sendChat(chatMessage);  // Sadece mesajı gönder
    }

}
