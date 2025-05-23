// ClientHandler.java
package network;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private PrintWriter out;
    private final String playerName;
    private final String color;
    private final GameRoom room;

    public ClientHandler(Socket socket, String playerName, String color, GameRoom room) {
        this.socket = socket;
        this.playerName = playerName;
        this.color = color;
        this.room = room;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("🧩 ClientHandler başlatıldı: " + playerName);

            room.broadcast("JOIN:" + playerName + ":" + color);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("📥 Sunucu aldı: " + message + " (gönderen: " + playerName + ")");

                if (message.startsWith("MOVE:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 4) {
                        String movePlayer = parts[1];
                        int from = Integer.parseInt(parts[2]);
                        int to = Integer.parseInt(parts[3]);
                        // SUNUCUDA GEÇERLİLİK KONTROLÜ
                        if (!room.getCurrentPlayerName().equals(playerName)) {
                            sendMessage("ERROR:Sıra " + room.getCurrentPlayerName() + "'da!");
                            continue;
                        }
                        if (room.isMoveValid(movePlayer, from, to)) {
                            room.applyMove(movePlayer, from, to);
                            room.broadcast("MOVE:" + movePlayer + ":" + from + ":" + to);
                            
                        } else {
                            sendMessage("ERROR:Geçersiz hamle! Zar veya taş kuralına uymuyor.");
                        }
                    }
                    continue; // Diğer if'lere bakmasın!
                }

                if (message.startsWith("ROLL:")) {
                    if (!room.getCurrentPlayerName().equals(playerName)) {
                        sendMessage("ERROR:Sıra sende değil!");
                        continue;
                    }
                    // Zarları SUNUCU atsın, client'ın gönderdiği zarları YOK SAY!
                    int die1 = (int) (Math.random() * 6) + 1;
                    int die2 = (int) (Math.random() * 6) + 1;
                    room.setDiceValues(die1, die2); // SUNUCUDAKİ GameManager'ın moveValues'u güncellensin!
                    room.broadcast("ROLL:" + playerName + ":" + die1 + ":" + die2);
                    System.out.println("🎲 Zar atıldı: " + playerName + " - " + die1 + ", " + die2);
                    continue;
                }

                if (message.startsWith("CHAT:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        String chatPlayer = parts[1];
                        String chatMessage = parts[2];
                        room.broadcast("CHAT:" + chatPlayer + ":" + chatMessage);
                        System.out.println("💬 Sohbet: " + chatPlayer + ": " + chatMessage);
                    }
                    continue;
                }

                if (message.startsWith("LEFT:")) {
                    room.broadcast("LEFT:" + playerName);
                    System.out.println("🚪 Oyuncu ayrıldı: " + playerName);
                    continue;
                }

                System.err.println("❓ Bilinmeyen mesaj türü: " + message);
            }
        } catch (IOException e) {
            System.err.println("❌ " + playerName + " bağlantı hatası: " + e.getMessage());
        } finally {
            room.removePlayer(this);
            try {
                socket.close();
                System.out.println("🔌 " + playerName + " bağlantısı kapatıldı.");
            } catch (IOException e) {
                System.err.println("❌ Socket kapatma hatası: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println(">>> [Server] " + playerName + " için mesaj gönderildi: " + message);  // 🧠 BU SATIR

        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public Socket getSocket() {
        return socket;
    }
}
