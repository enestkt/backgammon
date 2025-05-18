package network;

import java.io.*;
import java.net.*;
import java.util.*;

public class MultiClientServer {

    private static final int PORT = 5000;
    private static final List<GameRoom> gameRooms = Collections.synchronizedList(new ArrayList<>());
    private static final Queue<Socket> lobby = new LinkedList<>();

    public static void main(String[] args) {
        System.out.println("Çoklu Oyuncu Sunucusu başlatılıyor...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni bağlantı: " + clientSocket.getInetAddress());

                // Oyuncuyu lobiye ekle ve eşleştir
                addPlayerToLobby(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }

// Oyuncuyu lobiye ekle ve eşleştir
    private static void addPlayerToLobby(Socket playerSocket) {
        synchronized (lobby) {
            lobby.add(playerSocket);
            System.out.println("Lobiye oyuncu eklendi: " + playerSocket.getInetAddress());

            if (lobby.size() >= 2) {
                // İki oyuncu varsa yeni bir oyun odası oluştur
                Socket player1 = lobby.poll();
                Socket player2 = lobby.poll();
                GameRoom room = new GameRoom();
                gameRooms.add(room);

                // Odaya oyuncuları ekle
                room.addPlayer(player1, "Player 1", "WHITE");
                room.addPlayer(player2, "Player 2", "BLACK");

                System.out.println("Oyun odası oluşturuldu: " + room.getRoomID());

                // Oyunculara oyun başladığını bildir
                broadcastMessage(room, "START:Player 1:WHITE");
                broadcastMessage(room, "START:Player 2:BLACK");

                // Sıra Player 1'de başlar
                broadcastMessage(room, "TURN:Player 1");

                // Her iki oyuncuyu da dinlemeye başla
                new Thread(() -> handleClient(player1, room)).start();
                new Thread(() -> handleClient(player2, room)).start();
            }
        }
    }

    // İstemci bağlantısını işleme
    private static void handleClient(Socket clientSocket, GameRoom assignedRoom) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Sunucuya gelen mesaj: " + message);
                broadcastMessage(assignedRoom, message);  // Mesajı tüm oyunculara yayınla
            }
        } catch (IOException e) {
            System.err.println("İstemci bağlantı hatası: " + e.getMessage());
        }
    }

    // Oyun odasına mesaj gönderme
    private static void broadcastMessage(GameRoom room, String message) {
        synchronized (room) {
            for (Socket playerSocket : room.getPlayers()) {
                try {
                    PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                    out.println(message);
                    System.out.println("Mesaj gönderildi: " + message);
                } catch (IOException e) {
                    System.err.println("Mesaj gönderme hatası: " + e.getMessage());
                }
            }
        }
    }
}
