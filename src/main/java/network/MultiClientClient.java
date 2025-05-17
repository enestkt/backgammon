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
            startListening(); // Gelen mesajları dinlemeye başla
        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
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

    // Gelen mesajları işleme
    private void processMessage(String message) {
        try {
            if (message.startsWith("JOIN:")) {
                String playerName = message.substring(5);
                System.out.println("Oyuncu katıldı: " + playerName);
            } else if (message.startsWith("CHAT:")) {
                String chatMessage = message.substring(5);
                System.out.println("Sohbet: " + chatMessage);
            } else if (message.startsWith("MOVE:")) {
                String moveDetails = message.substring(5);
                System.out.println("Hamle: " + moveDetails);
            } else if (message.startsWith("ROLL:")) {
                String diceValue = message.substring(5);
                System.out.println("Zar atıldı: " + diceValue);
            } else if (message.startsWith("LEFT:")) {
                String playerName = message.substring(5);
                System.out.println("Oyuncu ayrıldı: " + playerName);
            } else {
                System.out.println("Sunucudan gelen: " + message);
            }

        } catch (Exception e) {
            System.err.println("Mesaj işleme hatası: " + e.getMessage());
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
    public void sendRoll(int die1, int die2) {
        sendMessage("ROLL:" + die1 + ":" + die2);
    }

    // Hamle mesajı gönderme
    public void sendMove(int from, int to) {
        sendMessage("MOVE:" + from + ":" + to);
    }

    // Sohbet mesajı gönderme
    public void sendChat(String chatMessage) {
        sendMessage("CHAT:" + chatMessage);
    }

    // Oyuncu ayrılma mesajı gönderme
    public void sendLeave() {
        sendMessage("LEFT:" + socket.getInetAddress());
    }

    // Bağlantıyı kapatma
    public void close() {
        try {
            if (socket != null) {
                sendLeave();
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
