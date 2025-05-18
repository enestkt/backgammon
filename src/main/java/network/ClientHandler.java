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
            room.broadcast("JOIN:" + playerName + ":" + color);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MOVE:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 4) {
                        String movePlayer = parts[1];
                        int from = Integer.parseInt(parts[2]);
                        int to = Integer.parseInt(parts[3]);
                        room.broadcast("MOVE:" + movePlayer + ":" + from + ":" + to);
                        System.out.println("Hamle: " + movePlayer + " - " + from + " -> " + to);
                    }
                } else if (message.startsWith("CHAT:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        String chatPlayer = parts[1];
                        String chatMessage = parts[2];
                        room.broadcast("CHAT:" + chatPlayer + ":" + chatMessage);
                        System.out.println("Sohbet: " + chatPlayer + ": " + chatMessage);
                    }
                } else if (message.startsWith("ROLL:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 4) {
                        String rollPlayer = parts[1];
                        int die1 = Integer.parseInt(parts[2]);
                        int die2 = Integer.parseInt(parts[3]);
                        room.broadcast("ROLL:" + rollPlayer + ":" + die1 + ":" + die2);
                        System.out.println("Zar atıldı: " + rollPlayer + " - " + die1 + ", " + die2);
                    }
                } else if (message.startsWith("LEFT:")) {
                    room.broadcast("LEFT:" + playerName);
                    System.out.println(playerName + " oyundan ayrıldı!");
                }
            }
        } catch (IOException e) {
            System.err.println(playerName + " bağlantı hatası: " + e.getMessage());
        } finally {
            room.removePlayer(this);
            try {
                socket.close();
                System.out.println(playerName + " bağlantısı kapatıldı.");
            } catch (IOException e) {
                System.err.println("Socket kapatma hatası: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public Socket getSocket() {
        return socket;
    }
}
