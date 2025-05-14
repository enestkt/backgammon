package ui;

import logic.GameManager;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.Client;

public class GameFrame extends JFrame {

    private final GameManager gameManager;
    private final GamePanel gamePanel;
    private final InfoPanel infoPanel;

    public GameFrame(GameManager gameManager, boolean isOnline) throws IOException {
        this.gameManager = gameManager;

        setTitle("Tavla - Oyun");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 650); // Genişlik artırıldı
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Oyun panelini oluştur
        gamePanel = new GamePanel(gameManager);

        // Online veya offline modda info panelini oluştur
        if (isOnline) {
            Client client = new Client("127.0.0.1", 5000); // Sunucuya bağlan
            infoPanel = new InfoPanel(gameManager, client);
        } else {
            infoPanel = new InfoPanel(gameManager, null); // Offline modda client yok
        }

        gamePanel.setInfoPanel(infoPanel); // Bilgi panelini bağla

        setLayout(new BorderLayout());
        add(infoPanel, BorderLayout.WEST);
        add(gamePanel, BorderLayout.CENTER);

        setVisible(true);
    }
}
