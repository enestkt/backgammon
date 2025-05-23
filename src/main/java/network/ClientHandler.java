package network;

import java.io.*;
import java.net.*;

/**
 * ClientHandler sınıfı, her bir istemciyi (oyuncuyu) temsil eden ve 
 * istemcinin sunucu ile iletişimini yöneten iş parçacığıdır (Thread).
 * 
 * - Oyuncudan gelen mesajları dinler ve işler.
 * - Sıra kontrolü yapar, hamleleri ve zar atışlarını yönetir.
 * - Oda (GameRoom) ile bağlantı sağlar ve mesajları iletir.
 */
public class ClientHandler implements Runnable {

    // Oyuncunun bağlantı soketi
    private final Socket socket;

    // Mesaj göndermek için kullanılan PrintWriter
    private PrintWriter out;

    // Oyuncunun adı
    private final String playerName;

    // Oyuncunun taş rengi (string olarak)
    private final String color;

    // Bu oyuncunun ait olduğu oda (GameRoom)
    private final GameRoom room;

    /**
     * ClientHandler yapıcı metodu. 
     * Her istemci bağlandığında, onun için yeni bir ClientHandler oluşturulur.
     * 
     * @param socket Oyuncunun bağlantı soketi
     * @param playerName Oyuncunun adı
     * @param color Oyuncunun rengi
     * @param room Oyuncunun ait olduğu oyun odası
     */
    public ClientHandler(Socket socket, String playerName, String color, GameRoom room) {
        this.socket = socket;
        this.playerName = playerName;
        this.color = color;
        this.room = room;
    }

    /**
     * Thread olarak çalışır. İstemciden gelen mesajları dinler ve uygun şekilde işler.
     * Oyun mesajlarını kontrol edip, kurallara uygun olarak sunucuya ya da diğer oyuncuya iletir.
     */
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("🧩 ClientHandler başlatıldı: " + playerName);

            // Odaya yeni oyuncu katıldığında, bunu tüm oyunculara yayınla
            room.broadcast("JOIN:" + playerName + ":" + color);

            String message;
            // İstemciden mesaj geldikçe döngüde işle
            while ((message = in.readLine()) != null) {
                System.out.println("📥 Sunucu aldı: " + message + " (gönderen: " + playerName + ")");

                // ---- MOVE_BAR EKLENDİ ----
                if (message.startsWith("MOVE_BAR:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 2) {
                        int to = Integer.parseInt(parts[1]);
                        // Sıra kontrolü
                        if (!room.getCurrentPlayerName().equals(playerName)) {
                            sendMessage("ERROR:Sıra " + room.getCurrentPlayerName() + "'da!");
                            continue;
                        }
                        // Hamle kuralı kontrolü
                        if (room.isBarMoveValid(playerName, to)) {
                            room.applyBarMove(playerName, to);
                            room.broadcast("MOVE_BAR:" + playerName + ":" + to);
                        } else {
                            sendMessage("ERROR:Bar’dan taş çıkarılamıyor.");
                        }
                    }
                    continue;
                }
                // -------------------------

                // Taş hareketi (normal hamle) mesajı
                if (message.startsWith("MOVE:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 4) {
                        String movePlayer = parts[1];
                        int from = Integer.parseInt(parts[2]);
                        int to = Integer.parseInt(parts[3]);
                        // Sıra kontrolü
                        if (!room.getCurrentPlayerName().equals(playerName)) {
                            sendMessage("ERROR:Sıra " + room.getCurrentPlayerName() + "'da!");
                            continue;
                        }
                        // Geçerli hamle mi kontrolü
                        if (room.isMoveValid(movePlayer, from, to)) {
                            room.applyMove(movePlayer, from, to);
                            room.broadcast("MOVE:" + movePlayer + ":" + from + ":" + to);
                        } else {
                            sendMessage("ERROR:Geçersiz hamle! Zar veya taş kuralına uymuyor.");
                        }
                    }
                    continue;
                }

                // Zar atma mesajı
                if (message.startsWith("ROLL:")) {
                    if (!room.getCurrentPlayerName().equals(playerName)) {
                        sendMessage("ERROR:Sıra sende değil!");
                        continue;
                    }
                    int die1 = (int) (Math.random() * 6) + 1;
                    int die2 = (int) (Math.random() * 6) + 1;
                    room.setDiceValues(die1, die2);
                    room.broadcast("ROLL:" + playerName + ":" + die1 + ":" + die2);
                    System.out.println("🎲 Zar atıldı: " + playerName + " - " + die1 + ", " + die2);
                    continue;
                }

                // Oyuncu ayrıldığında
                if (message.startsWith("LEFT:")) {
                    room.broadcast("LEFT:" + playerName);
                    System.out.println("🚪 Oyuncu ayrıldı: " + playerName);
                    continue;
                }

                // Bilinmeyen mesaj türleri için uyarı
                System.err.println("❓ Bilinmeyen mesaj türü: " + message);
            }
        } catch (IOException e) {
            System.err.println("❌ " + playerName + " bağlantı hatası: " + e.getMessage());
        } finally {
            // Bağlantı kapatıldığında oyuncuyu odadan çıkar ve soketi kapat
            room.removePlayer(this);
            try {
                socket.close();
                System.out.println("🔌 " + playerName + " bağlantısı kapatıldı.");
            } catch (IOException e) {
                System.err.println("❌ Socket kapatma hatası: " + e.getMessage());
            }
        }
    }

    /**
     * Bu istemciye mesaj göndermek için kullanılır.
     * @param message Gönderilecek mesaj metni
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println(">>> [Server] " + playerName + " için mesaj gönderildi: " + message);
        }
    }

    /**
     * Oyuncu adını döndürür.
     * @return playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * İstemcinin bağlantı soketini döndürür.
     * @return socket
     */
    public Socket getSocket() {
        return socket;
    }
}
