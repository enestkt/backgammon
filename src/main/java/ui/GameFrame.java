package ui;

import logic.GameManager;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.MultiClientClient;

/**
 * GameFrame, tavla oyununun ana oyun ekranını ve Swing GUI bileşenlerini oluşturur.
 * Oyun paneli, bilgi paneli ve sunucu ile iletişimi burada yönetir.
 */
public class GameFrame extends JFrame {

    // Oyun mantığını yöneten ana sınıf
    private final GameManager gameManager;

    // Tahta ve taşların çizildiği panel
    private final GamePanel gamePanel;

    // Oyuncu, zar ve bar bilgisi gösteren panel
    private final InfoPanel infoPanel;

    // Ağ üzerinden sunucuya bağlantı (Online modda kullanılır)
    private final MultiClientClient client;

    // Kendi oyuncu adını tutar
    private String myPlayerName;

    // Sıra sende mi kontrolü
    private boolean isMyTurn = false;

    /**
     * GameFrame yapıcı metodu.
     * @param gameManager Oyun mantığı sınıfı
     * @param client Sunucuya bağlı istemci (offline modda null geçilebilir)
     */
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
        gamePanel.setClient(client);

        add(infoPanel, BorderLayout.WEST);
        add(gamePanel, BorderLayout.CENTER);

        setVisible(true);
        startListeningForUpdates();
    }

    /**
     * Sunucudan gelen güncellemeleri dinler ve mesajı işleyerek GUI'yi günceller.
     */
    private void startListeningForUpdates() {
        new Thread(() -> {
            while (true) {
                String message = client.receiveMessage();
                if (message == null) {
                    System.err.println("Bağlantı kesildi, dinleme sonlandırıldı.");
                    break;
                }
                client.processMessage(message);
                SwingUtilities.invokeLater(() -> processMessage(message));
            }
        }).start();
    }

    /**
     * Sunucudan veya rakipten gelen mesajı çözer ve GUI'yi günceller.
     * @param message Gelen mesaj
     */
    private void processMessage(String message) {
        try {
            if (message.startsWith("START:")) {
                String[] parts = message.split(":");
                myPlayerName = parts[1];
                System.out.println("myPlayerName SET: " + myPlayerName);
                infoPanel.updateInfo();

            } else if (message.startsWith("TURN:")) {
                String turnPlayer = message.substring(5).split(":")[0].trim();
                isMyTurn = turnPlayer.equals(myPlayerName);
                gameManager.setCurrentPlayerByName(turnPlayer);
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

            } else if (message.startsWith("ERROR:")) {
                JOptionPane.showMessageDialog(this, message.substring(6), "Hata", JOptionPane.ERROR_MESSAGE);
                infoPanel.updateInfo();
                gamePanel.clearSelections();
                gamePanel.repaint();
            }
        } catch (Exception e) {
            System.err.println("Mesaj işleme hatası: " + e.getMessage());
        }
    }

    /**
     * Oynanan hamleyi sunucuya iletir.
     * @param from Başlangıç noktası
     * @param to Bitiş noktası
     */
    public void sendMove(int from, int to) {
        client.sendMove(from, to);
    }

    /**
     * Zar atma isteğini sunucuya gönderir.
     * @param die1 Birinci zar
     * @param die2 İkinci zar
     */
    public void sendRollDice(int die1, int die2) {
        client.sendRoll(die1, die2);
    }
}
