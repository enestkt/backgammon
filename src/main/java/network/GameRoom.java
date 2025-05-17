package network;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameRoom {
    private final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>(2));
    private final int roomID;

    public GameRoom() {
        this.roomID = this.hashCode();
    }

    // Odaya oyuncu ekleme
    public synchronized void addPlayer(Socket clientSocket) {
        String playerName = "Player " + (players.size() + 1);
        ClientHandler clientHandler = new ClientHandler(clientSocket, playerName);
        players.add(clientHandler);
        new Thread(clientHandler).start();
        broadcast("JOIN:" + playerName);  // Oyuncunun katıldığını yayınla
        System.out.println("Oda ID: " + roomID + " - Oyuncu eklendi: " + playerName);
    }

    // Odanın dolu olup olmadığını kontrol etme
    public synchronized boolean isFull() {
        return players.size() >= 2;
    }

    // Oda ID'sini alma
    public int getRoomID() {
        return this.roomID;
    }

    // Oda içindeki oyuncu sayısını alma
    public int getPlayerCount() {
        return players.size();
    }

    // Odaya ait tüm oyuncuları döndür
    public synchronized List<Socket> getPlayers() {
        List<Socket> sockets = new ArrayList<>();
        for (ClientHandler player : players) {
            sockets.add(player.getSocket());
        }
        return sockets;
    }

    // Mesajı tüm oyunculara gönderme
    private synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    // İç sınıf: ClientHandler
    class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private final String playerName;

        public ClientHandler(Socket socket, String playerName) {
            this.socket = socket;
            this.playerName = playerName;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                broadcast("JOIN:" + playerName);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("MOVE:")) {
                        // Hamle mesajı: MOVE:Player1:from:to
                        String[] parts = message.split(":");
                        String moveDetails = parts[1] + ":" + parts[2] + ":" + parts[3];
                        broadcast("MOVE:" + playerName + ":" + moveDetails);
                        System.out.println("Hamle: " + moveDetails);
                    } else if (message.startsWith("CHAT:")) {
                        // Sohbet mesajı: CHAT:Player1:message
                        String chatMessage = message.substring(5);
                        broadcast("CHAT:" + playerName + ": " + chatMessage);
                        System.out.println("Sohbet: " + chatMessage);
                    } else if (message.startsWith("ROLL:")) {
                        // Zar atma mesajı: ROLL:Player1:die1:die2
                        String[] parts = message.split(":");
                        int die1 = Integer.parseInt(parts[1]);
                        int die2 = Integer.parseInt(parts[2]);
                        broadcast("ROLL:" + playerName + ":" + die1 + ":" + die2);
                        System.out.println("Zar atıldı: " + die1 + ", " + die2);
                    } else if (message.startsWith("LEFT:")) {
                        // Oyuncu ayrılma mesajı
                        broadcast("LEFT:" + playerName);
                        System.out.println(playerName + " oyundan ayrıldı!");
                    } else {
                        // Genel mesaj
                        broadcast(playerName + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.err.println(playerName + " bağlantı hatası: " + e.getMessage());
            } finally {
                players.remove(this);
                broadcast("LEFT:" + playerName);
                try {
                    socket.close();
                    System.out.println(playerName + " bağlantısı kapatıldı.");
                } catch (IOException e) {
                    System.err.println("Socket kapatma hatası: " + e.getMessage());
                }
            }
        }

        // Mesaj gönderme metodu
        private void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        // Oyuncu socket bilgisini döndürme
        public Socket getSocket() {
            return socket;
        }
    }
}
