package ui;

import logic.GameManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.Client;

public class GameFrame extends JFrame {

    private GameManager gameManager;
    private GamePanel gamePanel;
    private InfoPanel infoPanel;

    public GameFrame(GameManager gameManager) throws IOException {
        this.gameManager = gameManager;

        setTitle("Tavla - Oyun");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 650); // Genişlik artırıldı
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // İlk önce GamePanel'i boş infoPanel'le değil, sonra bağlayacağız
        gamePanel = new GamePanel(gameManager); // ← InfoPanel olmadan oluştur
        Client client = new Client("127.0.0.1", 5000); // Sunucuya bağlan
        infoPanel = new InfoPanel(gameManager, client);
        gamePanel.setInfoPanel(infoPanel);      // ← Sonradan bağla

        add(infoPanel, BorderLayout.WEST);
        add(gamePanel, BorderLayout.CENTER);

        setVisible(true);
    }
}
