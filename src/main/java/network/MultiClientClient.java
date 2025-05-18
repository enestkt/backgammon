package network;

import java.io.*;
import java.net.*;

public class MultiClientClient {

    private String serverIp;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;

    public MultiClientClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        try {
            socket = new Socket(serverIp, serverPort);
            playerName = "Player-" + socket.getLocalPort();
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Bağlandı: " + serverIp + ":" + serverPort);
            startListening();
        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
            close();
        }
    }
    // Mesaj alma metodu

    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Mesaj alma hatası: " + e.getMessage());
            return null;
        }
    }

    private void processMessage(String message) {
        try {
            if (message.startsWith("START:")) {
                String[] parts = message.split(":");
                String playerName = parts[1];
                String color = parts[2];
                System.out.println("🎮 Oyun Başladı - " + playerName + " olarak oynuyorsunuz (" + color + ")");
            } else if (message.startsWith("TURN:")) {
                String playerName = message.substring(5);
                System.out.println("🔄 Sıra: " + playerName);
            } else if (message.startsWith("MOVE:")) {
                String[] parts = message.split(":");
                String playerName = parts[1];
                int from = Integer.parseInt(parts[2]);
                int to = Integer.parseInt(parts[3]);
                System.out.println("🔄 Hamle (" + playerName + "): " + from + " -> " + to);
            } else if (message.startsWith("ROLL:")) {
                String[] parts = message.split(":");
                String playerName = parts[1];
                int die1 = Integer.parseInt(parts[2]);
                int die2 = Integer.parseInt(parts[3]);
                System.out.println("🎲 Zar atıldı (" + playerName + "): " + die1 + ", " + die2);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Mesaj işleme hatası: " + e.getMessage());
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Gelen mesaj: " + message);
                }
            } catch (IOException e) {
                System.err.println("Bağlantı kesildi: " + e.getMessage());
            } finally {
                close();
            }
        }).start();
    }

    // Sohbet mesajı gönderme
    public void sendChat(String playerName, String chatMessage) {
        sendMessage("CHAT:" + playerName + ":" + chatMessage);
    }

// Zar atma mesajı gönderme
    public void sendRoll(String playerName, int die1, int die2) {
        sendMessage("ROLL:" + playerName + ":" + die1 + ":" + die2);
    }

// Hareket mesajı gönderme
    public void sendMove(String playerName, int from, int to) {
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
