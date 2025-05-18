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

    // Odaya oyuncu ekleme (Oyuncu adı ve renk ile birlikte)
    public synchronized void addPlayer(Socket clientSocket, String playerName, String color) {
        ClientHandler clientHandler = new ClientHandler(clientSocket, playerName, color);
        players.add(clientHandler);
        new Thread(clientHandler).start();
        broadcast("JOIN:" + playerName + ":" + color);  // Oyuncunun katıldığını yayınla
        System.out.println("Oda ID: " + roomID + " - Oyuncu eklendi: " + playerName + " (" + color + ")");
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
        private final String color;

        public ClientHandler(Socket socket, String playerName, String color) {
            this.socket = socket;
            this.playerName = playerName;
            this.color = color;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                broadcast("JOIN:" + playerName + ":" + color);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("MOVE:")) {
                        // Hamle mesajı: MOVE:from:to
                        String[] parts = message.split(":");
                        if (parts.length == 4) {
                            String movePlayer = parts[1];
                            int from = Integer.parseInt(parts[2]);
                            int to = Integer.parseInt(parts[3]);
                            broadcast("MOVE:" + movePlayer + ":" + from + ":" + to);
                            System.out.println("Hamle: " + movePlayer + " - " + from + " -> " + to);
                        } else {
                            System.err.println("Geçersiz hamle formatı: " + message);
                        }
                    } else if (message.startsWith("CHAT:")) {
                        // Sohbet mesajı: CHAT:playerName:message
                        String[] parts = message.split(":", 3);
                        if (parts.length == 3) {
                            String chatPlayer = parts[1];
                            String chatMessage = parts[2];
                            broadcast("CHAT:" + chatPlayer + ":" + chatMessage);
                            System.out.println("Sohbet: " + chatPlayer + ": " + chatMessage);
                        }
                    } else if (message.startsWith("ROLL:")) {
                        // Zar atma mesajı: ROLL:playerName:die1:die2
                        String[] parts = message.split(":");
                        if (parts.length == 4) {
                            String rollPlayer = parts[1];
                            int die1 = Integer.parseInt(parts[2]);
                            int die2 = Integer.parseInt(parts[3]);
                            broadcast("ROLL:" + rollPlayer + ":" + die1 + ":" + die2);
                            System.out.println("Zar atıldı: " + rollPlayer + " - " + die1 + ", " + die2);
                        } else {
                            System.err.println("Geçersiz zar formatı: " + message);
                        }
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

        // Sıra değiştirme metodu
        public synchronized void switchTurn(String currentPlayer) {
            String nextPlayer = currentPlayer.equals("Player 1") ? "Player 2" : "Player 1";
            broadcast("TURN:" + nextPlayer);
            System.out.println("Sıra geçti: " + nextPlayer);
        }
    }
}
