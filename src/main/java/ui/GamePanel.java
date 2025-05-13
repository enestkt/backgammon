package ui;

import logic.GameManager;
import model.Color;
import model.Player;
import model.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import ui.InfoPanel;
import ui.MainFrame;

public class GamePanel extends JPanel {

    private final GameManager gameManager;
    private int selectedPointIndex = -1;
    private boolean barCheckerSelected = false;
    private InfoPanel infoPanel;

    public GamePanel(GameManager gameManager) {
        this.gameManager = gameManager;
        setBackground(new java.awt.Color(200, 200, 200));  // Nötr arka plan rengi,

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedIndex = getClickedPointIndex(e.getX(), e.getY());

                if (gameManager.hasBarChecker(gameManager.getCurrentPlayer().getColor())) {
                    if (barCheckerClicked(e.getX(), e.getY())) {
                        barCheckerSelected = true;
                        repaint();
                        return;
                    }

                    if (barCheckerSelected && clickedIndex != -1 && gameManager.moveFromBar(clickedIndex)) {
                        barCheckerSelected = false;
                        repaint();
                    }
                    return;
                }

                if (clickedIndex == -1) {
                    return;
                }

                if (selectedPointIndex == -1) {
                    Point point = gameManager.getBoard().getPoint(clickedIndex);
                    if (!point.isEmpty() && point.getColor() == gameManager.getCurrentPlayer().getColor()) {
                        selectedPointIndex = clickedIndex;
                    }
                } else {
                    boolean moved = gameManager.moveChecker(selectedPointIndex, clickedIndex);
                    selectedPointIndex = -1;

                    if (moved) {
                        infoPanel.updateInfo();
                        repaint();
                        checkGameOver();
                    }
                }
                repaint();
            }
        });
    }

    public void setInfoPanel(InfoPanel infoPanel) {
        this.infoPanel = infoPanel;
    }

    public void updateDiceValues() {
        repaint();
    }

    private void checkGameOver() {
        Player current = gameManager.getCurrentPlayer();
        if (gameManager.hasWon(current)) {
            JOptionPane.showMessageDialog(this, current.getName() + " oyunu kazandı! 🎉", "Tebrikler", JOptionPane.INFORMATION_MESSAGE);
            int response = JOptionPane.showConfirmDialog(this, "Yeni oyun başlatılsın mı?", "Yeniden Başlat", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(() -> new MainFrame());
            }
            System.exit(0);
        }
    }

    private int getClickedPointIndex(int x, int y) {
        int width = getWidth();
        int height = getHeight();
        int barWidth = 40;
        int triangleWidth = (width - barWidth) / 12;

        if (y < height / 2) {
            for (int i = 0; i < 12; i++) {
                int px = (i < 6) ? i * triangleWidth : i * triangleWidth + barWidth;
                if (x >= px && x <= px + triangleWidth) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < 12; i++) {
                int index = 23 - i;
                int px = (i < 6) ? i * triangleWidth : i * triangleWidth + barWidth;
                if (x >= px && x <= px + triangleWidth) {
                    return index;
                }
            }
        }
        return -1;
    }

    private boolean barCheckerClicked(int x, int y) {
        int diameter = 40;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = diameter / 2;
        return Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= radius * radius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawBarChecker(g);
        drawBarTargets(g);
        drawBorneOff(g);
    }

    private void drawBoard(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int barWidth = 40;
        int triangleWidth = (width - barWidth) / 12;
        int triangleHeight = height / 2 - 20;
        int middleX = width / 2 - barWidth / 2;

        // Orta Barı Çiz
        g.setColor(convertToAwtColor(Color.GRAY));
        g.fillRect(middleX, 0, barWidth, height);

        // Üst Üçgenleri Çiz
        for (int i = 0; i < 12; i++) {
            int x = (i < 6) ? i * triangleWidth : i * triangleWidth + barWidth;
            int[] xPoints = {x, x + triangleWidth / 2, x + triangleWidth};
            int[] yPoints = {0, triangleHeight, 0};
            g.setColor((i % 2 == 0) ? convertToAwtColor(Color.DARK_GRAY) : convertToAwtColor(Color.ORANGE));
            g.fillPolygon(xPoints, yPoints, 3);
            drawCheckers(g, i, x, 5, triangleWidth, true);
        }

        // Alt Üçgenleri Çiz
        for (int i = 0; i < 12; i++) {
            int index = 23 - i;
            int x = (i < 6) ? i * triangleWidth : i * triangleWidth + barWidth;
            int[] xPoints = {x, x + triangleWidth / 2, x + triangleWidth};
            int[] yPoints = {getHeight(), getHeight() - triangleHeight, getHeight()};
            g.setColor((i % 2 == 0) ? convertToAwtColor(Color.DARK_GRAY) : convertToAwtColor(Color.ORANGE));
            g.fillPolygon(xPoints, yPoints, 3);
            drawCheckers(g, index, x, getHeight() - 5, triangleWidth, false);
        }
    }

    private void drawCheckers(Graphics g, int pointIndex, int xStart, int yStart, int triangleWidth, boolean top) {
        Point point = gameManager.getBoard().getPoint(pointIndex);
        if (point.isEmpty()) {
            return;
        }

        Color modelColor = point.getColor();
        java.awt.Color baseColor = convertToAwtColor(modelColor);
        int count = point.getCount();
        int diameter = Math.min(triangleWidth - 6, 40);
        int spacing = diameter + 4;
        int x = xStart + (triangleWidth - diameter) / 2;

        for (int i = 0; i < count; i++) {
            int y = top ? yStart + i * spacing : yStart - (i + 1) * spacing;

            // 🟥 Eğer bu taş seçiliyse kırmızı renkte çiz
            if (pointIndex == selectedPointIndex && i == count - 1) {
                g.setColor(java.awt.Color.RED); // Kırmızı renkte dolgu
                g.fillOval(x - 2, y - 2, diameter + 4, diameter + 4); // Biraz daha büyük çizerek vurgula
            } else {
                // Ana taşın çizimi
                g.setColor(baseColor);
                g.fillOval(x, y, diameter, diameter);
            }

            // Kenarlık rengi (kontrast olacak şekilde)
            g.setColor(baseColor == java.awt.Color.WHITE ? java.awt.Color.BLACK : java.awt.Color.WHITE);
            g.drawOval(x, y, diameter, diameter);
        }
    }

    private void drawBarChecker(Graphics g) {
        Color currentColor = gameManager.getCurrentPlayer().getColor();
        if (!gameManager.hasBarChecker(currentColor)) {
            return;
        }

        java.awt.Color drawColor = convertToAwtColor(currentColor);
        g.setColor(drawColor);
        int diameter = 40;
        int x = getWidth() / 2 - diameter / 2;
        int y = getHeight() / 2 - diameter / 2;

        // 🔴 Yutulan taşa tıklanmışsa kırmızı olarak çiz
        if (barCheckerSelected) {
            g.setColor(java.awt.Color.RED);
            g.fillOval(x - 2, y - 2, diameter + 4, diameter + 4);
        }

        // Yutulan taşın ana rengi
        g.setColor(drawColor);
        g.fillOval(x, y, diameter, diameter);
        g.setColor(drawColor == java.awt.Color.WHITE ? java.awt.Color.BLACK : java.awt.Color.WHITE);
        g.drawOval(x, y, diameter, diameter);
    }

    private void drawBarTargets(Graphics g) {
        Color currentColor = gameManager.getCurrentPlayer().getColor();

        // Sadece yutulan taş varsa göster
        if (!gameManager.hasBarChecker(currentColor)) {
            return;
        }

        List<Integer> validTargets = gameManager.getBarEntryTargets(currentColor);
        int width = getWidth();
        int height = getHeight();
        int barWidth = 40;
        int triangleWidth = (width - barWidth) / 12;

        for (int index : validTargets) {
            int i = (index < 12) ? index : 23 - index;
            int x = (i < 6) ? i * triangleWidth : i * triangleWidth + barWidth;
            int xCenter = x + triangleWidth / 2;
            int y = (index < 12) ? 5 : height - 45;

            // 🌀 Yutulan taş geri dönerken gidilebilecek noktaları hafif Cyan ile göster
            g.setColor(new java.awt.Color(0, 255, 255, 100));  // Şeffaf Cyan
            g.fillOval(xCenter - 20, y - 20, 40, 40);  // Noktanın büyüklüğünü arttırdık
        }
    }

    private void drawBorneOff(Graphics g) {
        int x = getWidth() - 50;
        int y = 20;
        int diameter = 30;

        for (int i = 0; i < gameManager.getBoard().getBorneOff(Color.WHITE); i++) {
            g.setColor(convertToAwtColor(Color.WHITE));

            g.fillOval(x, y + i * (diameter + 5), diameter, diameter);
        }

        for (int i = 0; i < gameManager.getBoard().getBorneOff(Color.BLACK); i++) {
            g.setColor(convertToAwtColor(Color.BLACK));
            g.fillOval(x, y + 200 + i * (diameter + 5), diameter, diameter);
        }
    }

    private java.awt.Color convertToAwtColor(Color color) {
        return switch (color) {
            case WHITE ->
                java.awt.Color.WHITE;
            case BLACK ->
                java.awt.Color.BLACK;
            case LIGHT_GRAY ->
                java.awt.Color.LIGHT_GRAY;
            case DARK_GRAY ->
                java.awt.Color.DARK_GRAY;
            case ORANGE ->
                java.awt.Color.ORANGE;
            case GRAY ->
                java.awt.Color.GRAY;
            case CYAN ->
                java.awt.Color.CYAN;
            default ->
                java.awt.Color.BLACK; // Varsayılan renk
        };
    }

}
