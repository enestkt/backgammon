package network;

import java.io.*;
import java.net.*;

public class MultiClientClient {

    private String serverIp;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MultiClientClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        try {
            socket = new Socket(serverIp, serverPort);
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

    // Gelen mesajları dinleme
    private void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    processMessage(message);
                }
            } catch (IOException e) {
                System.err.println("Bağlantı kesildi: " + e.getMessage());
            } finally {
                close();
            }
        }).start();
    }

    private void processMessage(String message) {
        try {
            if (message.startsWith("JOIN:")) {
                String playerName = message.substring(5);
                System.out.println("🔗 Oyuncu katıldı: " + playerName);
            } else if (message.startsWith("CHAT:")) {
                String[] parts = message.split(":");
                if (parts.length >= 3) {
                    String playerName = parts[1];
                    String chatMessage = parts[2];
                    System.out.println("💬 " + playerName + ": " + chatMessage);
                } else {
                    System.err.println("❌ Geçersiz sohbet formatı: " + message);
                }
            } else if (message.startsWith("MOVE:")) {
                String[] parts = message.split(":");
                if (parts.length == 4) {
                    String playerName = parts[1];
                    int from = Integer.parseInt(parts[2]);
                    int to = Integer.parseInt(parts[3]);
                    System.out.println("🔄 Hamle (" + playerName + "): " + from + " -> " + to);
                } else {
                    System.err.println("❌ Geçersiz hamle formatı: " + message);
                }
            } else if (message.startsWith("ROLL:")) {
                String[] parts = message.split(":");
                if (parts.length == 4) {
                    String playerName = parts[1];
                    int die1 = Integer.parseInt(parts[2]);
                    int die2 = Integer.parseInt(parts[3]);
                    System.out.println("🎲 Zar atıldı (" + playerName + "): " + die1 + ", " + die2);
                } else {
                    System.err.println("❌ Geçersiz zar formatı: " + message);
                }
            } else if (message.startsWith("LEFT:")) {
                String playerName = message.substring(5);
                System.out.println("🚪 Oyuncu ayrıldı: " + playerName);
            } else {
                System.out.println("🌐 Sunucudan gelen: " + message);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Mesaj işleme hatası: " + e.getMessage());
        }
    }

    // Mesaj gönderme
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Gönderilen mesaj: " + message);
        } else {
            System.err.println("Bağlantı yok, mesaj gönderilemedi.");
        }
    }

    // Zar atma mesajı gönderme
    // Zar atma mesajı gönderme
    public void sendRoll(String playerName, int die1, int die2) {
        sendMessage("ROLL:" + playerName + ":" + die1 + ":" + die2);
    }

    public void sendMove(String playerName, int from, int to) {
        sendMessage("MOVE:" + playerName + ":" + from + ":" + to);
    }

    // Sohbet mesajı gönderme
    public void sendChat(String playerName, String chatMessage) {
        sendMessage("CHAT:" + playerName + ": " + chatMessage);
    }

    // Oyuncu ayrılma mesajı gönderme
    public void sendLeave(String playerName) {
        sendMessage("LEFT:" + playerName);
    }

    // Bağlantıyı kapatma
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                sendLeave(socket.getInetAddress().toString());
                socket.close();
                System.out.println("Bağlantı kapatıldı.");
            }
        } catch (IOException e) {
            System.err.println("Bağlantı kapatma hatası: " + e.getMessage());
        }
    }

    // Sunucu IP'sini alma
    public String getServerIp() {
        return serverIp;
    }

    // Sunucu portunu alma
    public int getServerPort() {
        return serverPort;
    }
}
