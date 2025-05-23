package network;

import java.io.*;
import java.net.*;

/**
 * MultiClientClient, istemci tarafında sunucuya bağlanıp
 * mesaj gönderme/alma ve kendi oyuncu bilgisini tutma işini yapar.
 */
public class MultiClientClient {

    // Sunucu IP adresi ve portu
    private String serverIp;
    private int serverPort;

    // Ağ bağlantısı için socket ve IO nesneleri
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Oyuncu ve oyun durumu bilgileri
    private String playerName;       // Kullanıcının oyuncu adı
    private String playerColor;      // Kullanıcının rengi
    private String currentPlayer;    // Sırası olan oyuncunun adı

    /**
     * Sunucuya bağlanır ve gerekli IO akışlarını başlatır.
     * @param serverIp Sunucu IP adresi
     * @param serverPort Sunucu portu
     */
    public MultiClientClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        try {
            socket = new Socket(serverIp, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Bağlandı: " + serverIp + ":" + serverPort);
        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
            close();
        }
    }

    /**
     * Sunucudan gelen mesajı okur. (Ana dinleme fonksiyonu)
     * @return Okunan mesaj (String)
     */
    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Mesaj alma hatası: " + e.getMessage());
            close();
            return null;
        }
    }

    /**
     * Sunucudan veya rakipten gelen mesajı çözümler ve bilgileri günceller.
     * @param message Alınan mesaj
     */
    public void processMessage(String message) {
        try {
            if (message.startsWith("START:")) {
                String[] parts = message.split(":");
                playerName = parts[1];         // Örneğin Player 1
                playerColor = parts[2];        // Örneğin WHITE
                System.out.println("🎮 Oyun Başladı - " + playerName + " olarak oynuyorsunuz (" + playerColor + ")");
            } else if (message.startsWith("TURN:")) {
                currentPlayer = message.substring(5).trim(); // Sırası gelen oyuncunun adı
                System.out.println("🔄 Sıra: " + currentPlayer);
            }
            // Geri kalan oyun mesajları (MOVE, ROLL) GUI tarafında (GameFrame'de) işlenecek.
        } catch (Exception e) {
            System.err.println("⚠️ Mesaj işleme hatası: " + e.getMessage());
        }
    }

    /**
     * Zar atma mesajı gönderir.
     * @param die1 Birinci zar değeri
     * @param die2 İkinci zar değeri
     */
    public void sendRoll(int die1, int die2) {
        sendMessage("ROLL:");
    }

    /**
     * Taş hareketini sunucuya iletir.
     * @param from Taşın geldiği index
     * @param to   Taşın gideceği index
     */
    public void sendMove(int from, int to) {
        sendMessage("MOVE:" + playerName + ":" + from + ":" + to);
    }

    /**
     * Sunucuya düz mesaj gönderir.
     * @param message Gönderilecek mesaj
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Gönderilen mesaj: " + message);
        } else {
            System.err.println("Bağlantı yok, mesaj gönderilemedi.");
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Bağlantıyı kapatır.
     */
    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Bağlantı kapatıldı.");
            }
        } catch (IOException e) {
            System.err.println("Bağlantı kapatma hatası: " + e.getMessage());
        }
    }
}
