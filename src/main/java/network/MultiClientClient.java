// MultiClientClient.java - Mesaj Güncelleme ve Sıra Kontrolü

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
    private String currentPlayer;

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

    private void processMessage(String message) {
        try {
            if (message.startsWith("START:")) {
                String[] parts = message.split(":");
                
                for(String s: parts){
                    System.out.println(s+ "-");
                }
                
                playerName = parts[1];
                String color = parts[2];
                System.out.println("🎮 Oyun Başladı - " + playerName + " olarak oynuyorsunuz (" + color + ")");
            } else if (message.startsWith("TURN:")) {
                currentPlayer = message.substring(5);
                System.out.println("🔄 Sıra: " + currentPlayer);
                if (currentPlayer.equals(playerName)) {
                    System.out.println("🎯 Sıra sizde! Hamle yapabilirsiniz.");
                }
            } else if (message.startsWith("MOVE:")) {
                String[] parts = message.split(":");
                String movePlayer = parts[1];
                int from = Integer.parseInt(parts[2]);
                int to = Integer.parseInt(parts[3]);
                System.out.println("🔄 Hamle (" + movePlayer + "): " + from + " -> " + to);
            } else if (message.startsWith("ROLL:")) {
                String[] parts = message.split(":");
                String rollPlayer = parts[1];
                int die1 = Integer.parseInt(parts[2]);
                int die2 = Integer.parseInt(parts[3]);
                System.out.println("🎲 Zar atıldı (" + rollPlayer + "): " + die1 + ", " + die2);
            } else if (message.startsWith("ERROR:")) {
                System.err.println("⚠️ Hata: " + message.substring(6));
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
                    processMessage(message);
                }
            } catch (IOException e) {
                System.err.println("Bağlantı kesildi: " + e.getMessage());
            } finally {
                close();
            }
        }).start();
    }
    public String receiveMessage() {
    try {
        return in.readLine();
    } catch (IOException e) {
        System.err.println("Mesaj alma hatası: " + e.getMessage());
        close();
        return null;
    }
}


    public void sendChat(String chatMessage) {
        sendMessage("CHAT:" + playerName + ":" + chatMessage);
    }

    public void sendRoll(int die1, int die2) {
        if (playerName.equals(currentPlayer)) {
            sendMessage("ROLL:" + playerName + ":" + die1 + ":" + die2);
        } else {
            System.err.println("⚠️ Sıra sizde değil! Sadece " + currentPlayer + " zar atabilir.");
        }
    }

    public void sendMove(int from, int to) {
        if (playerName.equals(currentPlayer)) {
            sendMessage("MOVE:" + playerName + ":" + from + ":" + to);
        } else {
            System.err.println("⚠️ Sıra sizde değil! Sadece " + currentPlayer + " hamle yapabilir.");
        }
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
