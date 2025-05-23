package network;

import java.io.*;
import java.net.*;

public class MultiClientClient {

    private String serverIp;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;       // Sana ait oyuncu adı
    private String playerColor;      // Sana ait renk (WHITE veya BLACK)
    private String currentPlayer;    // Şu an hamle yapma sırası kimde

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

    // GameFrame içinde kullanacaksın: Sürekli mesaj dinle, gelen mesajı GameFrame.processMessage() ile işle
    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Mesaj alma hatası: " + e.getMessage());
            close();
            return null;
        }
    }

    // Mesajı çöz, kendi oyuncu adını ve sırayı güncelle
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
            // Geri kalan oyun mesajları (MOVE, ROLL, CHAT) GUI tarafında (GameFrame'de) işlenecek.
        } catch (Exception e) {
            System.err.println("⚠️ Mesaj işleme hatası: " + e.getMessage());
        }
    }

    // Oyun mesajları gönderimi (hepsinde kendi adını otomatik ekliyorsun)
    public void sendChat(String chatMessage) {
        sendMessage("CHAT:" + playerName + ":" + chatMessage);
    }

    public void sendRoll(int die1, int die2) {
       sendMessage("ROLL:");
    }

    public void sendMove(int from, int to) {
        sendMessage("MOVE:" + playerName + ":" + from + ":" + to);
    }

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