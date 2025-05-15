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

        // Oyun panelini oluştur
        gamePanel = new GamePanel(gameManager);

        // Online veya offline modda info panelini oluştur
        if (client != null) {
            infoPanel = new InfoPanel(gameManager, client);
            JOptionPane.showMessageDialog(this, "Bağlantı başarılı: " + client.getServerIp());
            startListeningForUpdates(); // Gelen mesajları dinlemeye başla
        } else {
            infoPanel = new InfoPanel(gameManager, null); // Offline modda client yok
        }

        gamePanel.setInfoPanel(infoPanel);
        add(infoPanel, BorderLayout.WEST);
        add(gamePanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Gelen mesajları dinleyerek oyun güncellemelerini işleyen metot
    private void startListeningForUpdates() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = client.receiveMessage();
                    if (message != null) {
                        SwingUtilities.invokeLater(() -> processMessage(message));
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Bağlantı kesildi: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    // Gelen mesajı işleme metodu
    private void processMessage(String message) {
        if (message.startsWith("MOVE:")) {
            // Hareket bilgisi
            String[] parts = message.split(":");
            int from = Integer.parseInt(parts[1]);
            int to = Integer.parseInt(parts[2]);
            gameManager.moveChecker(from, to);
            infoPanel.updateInfo();
            gamePanel.repaint();
        } else if (message.startsWith("CHAT:")) {
            // Sohbet mesajı
            String chatMessage = message.substring(5);
            infoPanel.updateChat(chatMessage);
        } else if (message.startsWith("ROLL:")) {
            // Zar bilgisi
            gameManager.rollDice();
            infoPanel.updateInfo();
        } else if (message.startsWith("SWITCH_TURN:")) {
            // Sıra değiştirme bilgisi
            gameManager.switchTurn();
            infoPanel.updateInfo();
        } else if (message.startsWith("BEAR_OFF:")) {
            // Taşı çıkarma (bearing off) bilgisi
            int fromIndex = Integer.parseInt(message.split(":")[1]);
            gameManager.tryBearOff(fromIndex);
            infoPanel.updateInfo();
            gamePanel.repaint();
        }
    }

    // Hareket bilgisini sunucuya gönder
    public void sendMove(int from, int to) {
        if (client != null) {
            client.sendMessage("MOVE:" + from + ":" + to);
        }
    }

    // Sohbet mesajını sunucuya gönder
    public void sendChatMessage(String chatMessage) {
        if (client != null) {
            client.sendMessage("CHAT:" + chatMessage);
        }
    }

    // Zar atma bilgisini sunucuya gönder
    public void sendRollDice() {
        if (client != null) {
            client.sendMessage("ROLL:");
        }
    }

    // Sıra değiştirme bilgisini sunucuya gönder
    public void sendSwitchTurn() {
        if (client != null) {
            client.sendMessage("SWITCH_TURN:");
        }
    }

    // Bearing off (taş çıkarma) bilgisini sunucuya gönder
    public void sendBearOff(int fromIndex) {
        if (client != null) {
            client.sendMessage("BEAR_OFF:" + fromIndex);
        }
    }
}
