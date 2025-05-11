package ui;

import logic.GameManager;
import model.Color;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class MainFrame extends JFrame {

    private JTextField whiteNameField;
    private JTextField blackNameField;

    public MainFrame() {
        setTitle("Tavla - Ana Menü");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1));

        JLabel title = new JLabel("🎲 Tavla Oyunu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton offlineButton = new JButton("Offline Başla");
        JButton onlineButton = new JButton("Online Oyna");

        offlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startOfflineGame();
            }
        });

        onlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ipAddress = JOptionPane.showInputDialog(null, "Sunucu IP adresini giriniz:", "127.0.0.1");
                if (ipAddress != null) {
                    startOnlineGame(ipAddress);
                }
            }
        });

        add(offlineButton);
        add(onlineButton);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        whiteNameField = new JTextField();
        blackNameField = new JTextField();
        inputPanel.add(new JLabel("Beyaz Oyuncu Adı:"));
        inputPanel.add(whiteNameField);
        inputPanel.add(new JLabel("Siyah Oyuncu Adı:"));
        inputPanel.add(blackNameField);
        add(inputPanel);

        JButton startButton = new JButton("Oyuna Başla");
        startButton.addActionListener(e -> startGame());
        add(startButton);

        setVisible(true);
    }

    private void startGame() {
        String white = whiteNameField.getText().trim();
        String black = blackNameField.getText().trim();

        if (white.isEmpty() || black.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Her iki oyuncu adını da girin.");
            return;
        }

        GameManager manager = new GameManager(white, black);
        JFrame animFrame = new JFrame("Taşlar Toplanıyor...");
        animFrame.setSize(500, 300);
        animFrame.setLocationRelativeTo(null);
        animFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AnimationPanel animPanel = new AnimationPanel(() -> {
            animFrame.dispose();

            try {
                new GameFrame(manager);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Bağlantı hatası: " + e.getMessage());
            }

        });

        animFrame.add(animPanel);
        animFrame.setVisible(true);
        dispose();
    }

    private void startOnlineGame(String ipAddress) {
        try {
            Socket clientSocket = new Socket(ipAddress, 5000);
            JOptionPane.showMessageDialog(null, "Bağlantı sağlandı! Oyuncu bağlandı.");
            System.out.println("Bağlantı sağlandı: " + clientSocket.getInetAddress());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Bağlantı hatası! Sunucuya ulaşılamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
            System.err.println("Bağlantı hatası: " + ex.getMessage());
        }
    }

    private void startOfflineGame() {
        String white = whiteNameField.getText().trim();
        String black = blackNameField.getText().trim();

        if (white.isEmpty() || black.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Her iki oyuncu adını da girin.");
            return;
        }

        GameManager manager = new GameManager(white, black);
        JFrame animFrame = new JFrame("Taşlar Toplanıyor...");
        animFrame.setSize(500, 300);
        animFrame.setLocationRelativeTo(null);
        animFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AnimationPanel animPanel = new AnimationPanel(() -> {
            animFrame.dispose();
            try {
                new GameFrame(manager);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Bağlantı hatası: " + e.getMessage());
            }

        });

        animFrame.add(animPanel);
        animFrame.setVisible(true);
        dispose(); // Ana menüyü kapat
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
