package ui;

import logic.GameManager;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.MultiClientClient;

/**
 * MainFrame, ana menü penceresini ve offline/online oyun başlatma ekranını oluşturur.
 * Oyuncu, oyun türünü seçebilir ve online bağlantı için IP girişi yapabilir.
 */
public class MainFrame extends JFrame {

    /**
     * MainFrame yapıcı metodu. Ana menü ekranını başlatır.
     */
    public MainFrame() {
        setTitle("Tavla - Ana Menü");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1));

        JLabel title = new JLabel("🎲 Tavla Oyunu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton offlineButton = new JButton("Offline Başla");
        JButton onlineButton = new JButton("Online Oyna");

        offlineButton.addActionListener(e -> startOfflineGame());
        onlineButton.addActionListener(e -> startOnlineGame());

        add(offlineButton);
        add(onlineButton);

        setVisible(true);
    }

    /**
     * Offline oyun başlatır (sunucuya gerek olmadan).
     */
    private void startOfflineGame() {
        try {
            GameManager manager = new GameManager("Player 1", "Player 2");
            new GameFrame(manager, null);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Oyun başlatma hatası: " + e.getMessage());
        }
    }

    /**
     * Online oyun başlatır ve kullanıcıdan sunucu IP adresini ister.
     */
    private void startOnlineGame() {
        String ipAddress = JOptionPane.showInputDialog(null, "Sunucu IP adresini giriniz:", "127.0.0.1");
        if (ipAddress == null || ipAddress.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bağlantı iptal edildi.");
            return;
        }

        try {
            GameManager manager = new GameManager("Player 1", "Player 2");
            MultiClientClient client = new MultiClientClient(ipAddress, 5000);
            new GameFrame(manager, client);
            setVisible(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Bağlantı hatası: " + e.getMessage());
        }
    }

    /**
     * Uygulama giriş noktası, ana menüyü başlatır.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
