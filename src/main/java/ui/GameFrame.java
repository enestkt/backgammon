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
    private String myPlayerName;
    private boolean isMyTurn = false;

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
        // Client referansını da gamePanel’e geçir:
        gamePanel.setClient(client);

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
                // Önce client’ın kendi state’ini güncelle
                client.processMessage(message);
                // Sonra GUI’yi güncelle
                SwingUtilities.invokeLater(() -> processMessage(message));
            }
        }).start();
    }

    private void processMessage(String message) {
        try {
            if (message.startsWith("START:")) {
                String[] parts = message.split(":");
                myPlayerName = parts[1];
                System.out.println("myPlayerName SET: " + myPlayerName);// "Player 1" veya "Player 2"
                infoPanel.updateInfo();

            } else if (message.startsWith("TURN:")) {
                String turnPlayer = message.substring(5).split(":")[0].trim();
                isMyTurn = turnPlayer.equals(myPlayerName);
                 gameManager.setCurrentPlayerByName(turnPlayer); // <-- EKLE
                System.out.println("myPlayerName: " + myPlayerName + " turnPlayer: " + turnPlayer + " isMyTurn: " + isMyTurn);
                infoPanel.setTurn(isMyTurn);
                gamePanel.setTurn(isMyTurn);
                infoPanel.updateInfo();

            } else if (message.startsWith("ROLL:")) {
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
                gameManager.forceMoveChecker(from, to);
                infoPanel.updateInfo();
                gamePanel.repaint();

            } else if (message.startsWith("CHAT:")) {
                infoPanel.updateChat(message.substring(5));

            } else if (message.startsWith("ERROR:")) {
                JOptionPane.showMessageDialog(this, message.substring(6), "Hata", JOptionPane.ERROR_MESSAGE);
                // --- EKLENECEK KISIM ---
                infoPanel.updateInfo();
                gamePanel.clearSelections();
                gamePanel.repaint();
            }
        } catch (Exception e) {
            System.err.println("Mesaj işleme hatası: " + e.getMessage());
        }
    }

    
    // Aşağıdaki methodlar GUI kodunu sadeleştiriyor:
    public void sendMove(int from, int to) {
        client.sendMove(from, to);
    }

    public void sendRollDice(int die1, int die2) {
        client.sendRoll(die1, die2);
    }

    public void sendChatMessage(String chatMessage) {
        client.sendChat(chatMessage);
    }
}
