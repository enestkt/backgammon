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
                if (message != null) {
                    SwingUtilities.invokeLater(() -> processMessage(message));
                }
            }
        }).start();
    }

    private void processMessage(String message) {
        try {
            if (message.startsWith("MOVE:")) {
                String[] parts = message.split(":");
                int from = Integer.parseInt(parts[1]);
                int to = Integer.parseInt(parts[2]);
                gameManager.moveChecker(from, to);
                infoPanel.updateInfo();
                gamePanel.repaint();
            } else if (message.startsWith("CHAT:")) {
                infoPanel.updateChat(message.substring(5));
            } else if (message.startsWith("ROLL:")) {
                infoPanel.updateChat("Zar atıldı: " + message.substring(5));
                gameManager.rollDice();
                infoPanel.updateInfo();
            } else if (message.startsWith("JOIN:")) {
                infoPanel.updateChat("Oyuncu katıldı: " + message.substring(5));
            } else if (message.startsWith("LEFT:")) {
                infoPanel.updateChat("Oyuncu ayrıldı: " + message.substring(5));
            } else {
                infoPanel.updateChat("Sunucudan: " + message);
            }
        } catch (Exception e) {
            System.err.println("Mesaj işleme hatası: " + e.getMessage());
        }
    }

    public void sendMove(int from, int to) {
        client.sendMessage("MOVE:" + from + ":" + to);
    }

    public void sendRollDice() {
        client.sendMessage("ROLL:");
    }

    public void sendChatMessage(String chatMessage) {
        client.sendMessage("CHAT:" + chatMessage);
    }

    public void sendLeave() {
        client.sendMessage("LEFT:");
    }
}
