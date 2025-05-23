// ClientHandler.java
package network;

import java.io.*;
import java.net.*;

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

                // ---- MOVE_BAR EKLENDİ ----
                if (message.startsWith("MOVE_BAR:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 2) {
                        int to = Integer.parseInt(parts[1]);
                        if (!room.getCurrentPlayerName().equals(playerName)) {
                            sendMessage("ERROR:Sıra " + room.getCurrentPlayerName() + "'da!");
                            continue;
                        }
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

                if (message.startsWith("MOVE:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 4) {
                        String movePlayer = parts[1];
                        int from = Integer.parseInt(parts[2]);
                        int to = Integer.parseInt(parts[3]);
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
                    continue;
                }

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

                // CHAT bloğu TAMAMEN KALDIRILDI!

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
            System.out.println(">>> [Server] " + playerName + " için mesaj gönderildi: " + message);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public Socket getSocket() {
        return socket;
    }
}
