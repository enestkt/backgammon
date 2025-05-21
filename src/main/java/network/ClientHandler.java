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

    @Override
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
                        if (!room.getCurrentPlayerName().equals(playerName)) {
                            sendMessage("ERROR:Sıra " + room.getCurrentPlayerName() + "'da!");
                            continue;
                        }
                        String movePlayer = parts[1];
                        int from = Integer.parseInt(parts[2]);
                        int to = Integer.parseInt(parts[3]);
                        room.broadcast("MOVE:" + movePlayer + ":" + from + ":" + to);
                        room.switchTurn();
                        System.out.println("✅ Hamle işlendi: " + movePlayer + " - " + from + " -> " + to);
                    } else {
                        System.err.println("❌ MOVE formatı hatalı: " + message);
                    }
                } else if (message.startsWith("ROLL:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 4) {
                        if (!room.getCurrentPlayerName().equals(playerName)) {
                            sendMessage("ERROR:Sıra " + room.getCurrentPlayerName() + "'da!");
                            continue;
                        }
                        String rollPlayer = parts[1];
                        int die1 = Integer.parseInt(parts[2]);
                        int die2 = Integer.parseInt(parts[3]);
                        room.broadcast("ROLL:" + rollPlayer + ":" + die1 + ":" + die2);
                        System.out.println("🎲 Zar atıldı: " + rollPlayer + " - " + die1 + ", " + die2);
                    } else {
                        System.err.println("❌ ROLL formatı hatalı: " + message);
                    }
                } else if (message.startsWith("CHAT:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        String chatPlayer = parts[1];
                        String chatMessage = parts[2];
                        room.broadcast("CHAT:" + chatPlayer + ":" + chatMessage);
                        System.out.println("💬 Sohbet: " + chatPlayer + ": " + chatMessage);
                    }
                } else if (message.startsWith("LEFT:")) {
                    room.broadcast("LEFT:" + playerName);
                    System.out.println("🚪 Oyuncu ayrıldı: " + playerName);
                } else {
                    System.err.println("❓ Bilinmeyen mesaj türü: " + message);
                }
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
