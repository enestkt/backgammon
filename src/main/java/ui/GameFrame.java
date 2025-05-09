package ui;

import logic.GameManager;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private GameManager gameManager;
    private GamePanel gamePanel;
    private InfoPanel infoPanel;

    public GameFrame(GameManager gameManager) {
        this.gameManager = gameManager;

        setTitle("Tavla - Oyun");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 650); // Genişlik artırıldı
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // İlk önce GamePanel'i boş infoPanel'le değil, sonra bağlayacağız
        gamePanel = new GamePanel(gameManager); // ← InfoPanel olmadan oluştur
        infoPanel = new InfoPanel(gameManager, gamePanel);
        gamePanel.setInfoPanel(infoPanel);      // ← Sonradan bağla

        add(infoPanel, BorderLayout.WEST);
        add(gamePanel, BorderLayout.CENTER);

        setVisible(true);
    }
}

