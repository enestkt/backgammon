package ui;

import logic.GameManager;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.MultiClientClient;

public class MainFrame extends JFrame {

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

    private void startOfflineGame() {
        try {
            GameManager manager = new GameManager("Player 1", "Player 2");
            new GameFrame(manager, null);  // Offline modda null geçiyoruz
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Oyun başlatma hatası: " + e.getMessage());
        }
    }

    private void startOnlineGame() {
        // Kullanıcıdan IP adresini al
        String ipAddress = JOptionPane.showInputDialog(null, "Sunucu IP adresini giriniz:", "127.0.0.1");

        // Eğer kullanıcı bir şey girmezse veya "Cancel" derse işlem iptal edilir
        if (ipAddress == null || ipAddress.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Bağlantı iptal edildi.");
            return;
        }

        try {
            // Bağlanmaya çalış
            GameManager manager = new GameManager("Player 1", "Player 2");
            // Client oluştur ve sunucuya bağlan
            MultiClientClient client = new MultiClientClient(ipAddress, 5000);
            new GameFrame(manager, client);  // Client bilgisini geçir
            setVisible(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Bağlantı hatası: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}