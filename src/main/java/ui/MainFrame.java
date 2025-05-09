package ui;

import logic.GameManager;
import model.Color;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTextField whiteNameField;
    private JTextField blackNameField;

    public MainFrame() {
        setTitle("Tavla - Oyuncu Girişi");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("🎲 Tavla Oyunu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        whiteNameField = new JTextField();
        blackNameField = new JTextField();
        inputPanel.add(new JLabel("Beyaz Oyuncu Adı:"));
        inputPanel.add(whiteNameField);
        inputPanel.add(new JLabel("Siyah Oyuncu Adı:"));
        inputPanel.add(blackNameField);
        add(inputPanel, BorderLayout.CENTER);

        JButton startButton = new JButton("Oyuna Başla");
        startButton.addActionListener(e -> startGame());
        add(startButton, BorderLayout.SOUTH);

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
        animFrame.dispose();         // animasyonu kapat
        new GameFrame(manager);     // oyunu başlat
    });

    animFrame.add(animPanel);
    animFrame.setVisible(true);

    dispose(); // oyuncu giriş penceresini kapat
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
