package ui;

import logic.GameManager;
import model.Color;
import model.Player;
import model.Point;
import network.MultiClientClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * GamePanel sınıfı, tavla tahtasını ve taşları çizer; oyuncunun tıklamalarını işleyip,
 * hamlelerin ve bar'dan taş çıkarmanın GUI tarafını yönetir.
 */
public class GamePanel extends JPanel {

    // Oyun mantığına erişim için GameManager referansı
    private final GameManager gameManager;

    // Seçili noktanın indeksi (kullanıcı taş seçince tutulur)
    private int selectedPointIndex = -1;

    // Bar'dan çıkarılacak taş seçili mi?
    private boolean barCheckerSelected = false;

    // Oyunun bilgi paneli (oyuncu/zar/bilgi)
    private InfoPanel infoPanel;

    // Sıra bu oyuncuda mı? (Kendi hamle hakkı mı)
    private boolean isMyTurn = false;

    // Sunucuya/diğer oyuncuya mesaj göndermek için istemci referansı
    private MultiClientClient client;

    /**
     * GamePanel yapıcı metodu.
     * Tahta arka planını ayarlar ve mouse ile kullanıcı tıklamalarını işler.
     * @param gameManager Oyun mantığına erişim
     */
    public GamePanel(GameManager gameManager) {
        this.gameManager = gameManager;
        setBackground(new java.awt.Color(200, 200, 200));

        // Tahtada tıklama olaylarını yönetir
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Sıra sende değilse veya istemci yoksa hiçbir şey yapma
                if (!isMyTurn || client == null) {
                    return;
                }
                int clickedIndex = getClickedPointIndex(e.getX(), e.getY());

                // Eğer bar'da taş varsa, sadece bar'dan çıkışa izin ver
                if (gameManager.hasBarChecker(gameManager.getCurrentPlayer().getColor())) {
                    // Yutulan taş (bar checker) simgesine tıklanırsa seçili yap
                    if (barCheckerClicked(e.getX(), e.getY())) {
                        barCheckerSelected = true;
                        selectedPointIndex = -1; // Önceki seçimi sıfırla
                        repaint();
                        return;
                    }
                    // Bar taşı seçilmişse ve tahtadaki uygun yere tıklanırsa hamle gönder
                    if (barCheckerSelected && clickedIndex != -1) {
                        client.sendMessage("MOVE_BAR:" + clickedIndex);
                        barCheckerSelected = false;
                        selectedPointIndex = -1; // Seçimleri sıfırla
                    }
                    return; // Bar'da taş varken başka işlem yapılmaz
                }

                // Bar'da taş yoksa klasik taş seçimi ve hamlesi:
                if (clickedIndex == -1) {
                    selectedPointIndex = -1;
                    repaint();
                    return;
                }

                // Henüz taş seçilmediyse: kendi taşını seçebilirsin
                if (selectedPointIndex == -1) {
                    Point point = gameManager.getBoard().getPoint(clickedIndex);
                    if (!point.isEmpty() && point.getColor() == gameManager.getCurrentPlayer().getColor()) {
                        selectedPointIndex = clickedIndex;
                        repaint();
                    }
                } else {
                    // Taş seçiliyse, hamleyi sunucuya gönder
                    client.sendMove(selectedPointIndex, clickedIndex);
                    selectedPointIndex = -1;
                    repaint();
                }
            }
        });
    }

    /**
     * GameFrame’den çağrılırken, online istemci referansı atanır.
     * @param client Sunucuya bağlı MultiClientClient nesnesi
     */
    public void setClient(MultiClientClient client) {
        this.client = client;
    }

    /**
     * Oyun bilgi paneli referansını atar.
     * @param infoPanel InfoPanel nesnesi
     */
    public void setInfoPanel(InfoPanel infoPanel) {
        this.infoPanel = infoPanel;
    }

    /**
     * Zar değerleri güncellenince paneli yeniden çizer.
     */
    public void updateDiceValues() {
        repaint();
    }

    /**
     * Sıra oyuncuda mı bilgisini ayarlar ve imleç tipini değiştirir.
     * @param isMyTurn Bu oyuncunun sırası mı?
     */
    public void setTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        setCursor(isMyTurn ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Tüm taş seçimlerini sıfırlar ve paneli yeniler.
     */
    public void clearSelections() {
        selectedPointIndex = -1;
        barCheckerSelected = false;
        repaint();
    }

    /**
     * Oyun bitişini kontrol eder, kazananı gösterir ve yeniden başlatma ister.
     * (Sadece örnek: Otomatik çağrılmıyor, kodda tetiklenirse çalışır.)
     */
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

    /**
     * Ekranda tıklanan koordinatın hangi point(index)'e denk geldiğini bulur.
     * @param x Tıklanan x koordinatı
     * @param y Tıklanan y koordinatı
     * @return Tahta index'i (0-23) veya -1 (geçersiz tıklama)
     */
    private int getClickedPointIndex(int x, int y) {
        int width = getWidth();
        int height = getHeight();
        int barWidth = 40;
        int triangleWidth = (width - barWidth) / 12;

        // Üst 12 noktadan biri mi?
        if (y < height / 2) {
            for (int i = 0; i < 12; i++) {
                int px = (i < 6) ? i * triangleWidth : i * triangleWidth + barWidth;
                if (x >= px && x <= px + triangleWidth) {
                    return i;
                }
            }
        } else { // Alt 12 noktadan biri mi?
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

    /**
     * Bar'daki taş (yutulan taş) simgesine tıklanıp tıklanmadığını kontrol eder.
     * @param x Tıklanan x
     * @param y Tıklanan y
     * @return Bar taşı tıklandıysa true
     */
    private boolean barCheckerClicked(int x, int y) {
        int diameter = 40;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = diameter / 2;
        return Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= radius * radius;
    }

    /**
     * Paneli çizerken çağrılır. Tahtayı, bar taşını, bar hedeflerini ve dışarı çıkan taşları çizer.
     * @param g Grafik nesnesi
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawBarChecker(g);
        drawBarTargets(g);
        drawBorneOff(g);
    }

    /**
     * Tavla tahtasının üst/alt üçgenlerini ve taşlarını çizer.
     * @param g Grafik nesnesi
     */
    private void drawBoard(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int barWidth = 40;
        int triangleWidth = (width - barWidth) / 12;
        int triangleHeight = height / 2 - 20;
        int middleX = width / 2 - barWidth / 2;

        // Orta Bar'ı (kırık taş alanı) çiz
        g.setColor(convertToAwtColor(Color.GRAY));
        g.fillRect(middleX, 0, barWidth, height);

        // Üstteki üçgenleri ve taşları çiz
        for (int i = 0; i < 12; i++) {
            int x = (i < 6) ? i * triangleWidth : i * triangleWidth + barWidth;
            int[] xPoints = {x, x + triangleWidth / 2, x + triangleWidth};
            int[] yPoints = {0, triangleHeight, 0};
            g.setColor((i % 2 == 0) ? convertToAwtColor(Color.DARK_GRAY) : convertToAwtColor(Color.ORANGE));
            g.fillPolygon(xPoints, yPoints, 3);
            drawCheckers(g, i, x, 5, triangleWidth, true);
        }

        // Alttaki üçgenleri ve taşları çiz
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

    /**
     * Belirtilen noktadaki taşları (beyaz/siyah) çizer.
     * Seçili taş kırmızı çerçeveyle vurgulanır.
     * @param g Grafik nesnesi
     * @param pointIndex Tahta noktası (0-23)
     * @param xStart X koordinatı
     * @param yStart Y koordinatı
     * @param triangleWidth Üçgen genişliği
     * @param top Üst mü (true) alt mı (false)
     */
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

            // Eğer bu taş seçiliyse kırmızı renkte çiz
            if (pointIndex == selectedPointIndex && i == count - 1) {
                g.setColor(java.awt.Color.RED);
                g.fillOval(x - 2, y - 2, diameter + 4, diameter + 4);
            } else {
                g.setColor(baseColor);
                g.fillOval(x, y, diameter, diameter);
            }

            // Kenar çizgisi (siyah/beyaz kontrast)
            g.setColor(baseColor == java.awt.Color.WHITE ? java.awt.Color.BLACK : java.awt.Color.WHITE);
            g.drawOval(x, y, diameter, diameter);
        }
    }

    /**
     * Bar'daki (yutulan) taşı orta noktada çizer.
     * Bar taşı seçildiyse kırmızı olarak vurgulanır.
     * @param g Grafik nesnesi
     */
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

        // Seçili ise kırmızıyla vurgula
        if (barCheckerSelected) {
            g.setColor(java.awt.Color.RED);
            g.fillOval(x - 2, y - 2, diameter + 4, diameter + 4);
        }

        g.setColor(drawColor);
        g.fillOval(x, y, diameter, diameter);
        g.setColor(drawColor == java.awt.Color.WHITE ? java.awt.Color.BLACK : java.awt.Color.WHITE);
        g.drawOval(x, y, diameter, diameter);
    }

    /**
     * Bar'daki taş için gidilebilecek (serbest) noktaları çizer.
     * Bar'dan taş çıkışı mümkün olan noktalar şeffaf cyan ile gösterilir.
     * @param g Grafik nesnesi
     */
    private void drawBarTargets(Graphics g) {
        Color currentColor = gameManager.getCurrentPlayer().getColor();

        // Yutulan taş yoksa gösterme
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

            // Serbest hedef noktaları şeffaf cyan ile çiz
            g.setColor(new java.awt.Color(0, 255, 255, 100));
            g.fillOval(xCenter - 20, y - 20, 40, 40);
        }
    }

    /**
     * Dışarı çıkarılmış (borne off) taşları sağ kenarda çizer.
     * @param g Grafik nesnesi
     */
    private void drawBorneOff(Graphics g) {
        int x = getWidth() - 50;
        int y = 20;
        int diameter = 30;

        // Beyaz taşlar için
        for (int i = 0; i < gameManager.getBoard().getBorneOff(Color.WHITE); i++) {
            g.setColor(convertToAwtColor(Color.WHITE));
            g.fillOval(x, y + i * (diameter + 5), diameter, diameter);
        }
        // Siyah taşlar için
        for (int i = 0; i < gameManager.getBoard().getBorneOff(Color.BLACK); i++) {
            g.setColor(convertToAwtColor(Color.BLACK));
            g.fillOval(x, y + 200 + i * (diameter + 5), diameter, diameter);
        }
    }

    /**
     * Modeldeki Color enum'unu awt.Color nesnesine dönüştürür.
     * @param color Model Color
     * @return java.awt.Color nesnesi
     */
    private java.awt.Color convertToAwtColor(Color color) {
        return switch (color) {
            case WHITE -> java.awt.Color.WHITE;
            case BLACK -> java.awt.Color.BLACK;
            case LIGHT_GRAY -> java.awt.Color.LIGHT_GRAY;
            case DARK_GRAY -> java.awt.Color.DARK_GRAY;
            case ORANGE -> java.awt.Color.ORANGE;
            case GRAY -> java.awt.Color.GRAY;
            case CYAN -> java.awt.Color.CYAN;
            default -> java.awt.Color.BLACK; // Varsayılan renk
        };
    }
}
