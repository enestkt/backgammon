package network;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * MultiClientServer, sunucu tarafında çoklu oyunculu odaları yönetir.
 * Her iki oyuncu geldiğinde yeni bir oyun odası (GameRoom) oluşturur ve oyunu başlatır.
 */
public class MultiClientServer {

    // Sunucu portu
    private static final int PORT = 5000;

    // Tüm oyun odalarını tutan liste
    private static final List<GameRoom> gameRooms = Collections.synchronizedList(new ArrayList<>());

    // Lobiye alınan oyuncuları (eşleştirme bekleyen) tutar
    private static final Queue<Socket> lobby = new LinkedList<>();

    /**
     * Sunucunun ana fonksiyonu. Bağlantıları kabul eder ve oyuncuları lobiye ekler.
     */
    public static void main(String[] args) {
        System.out.println("Çoklu Oyuncu Sunucusu başlatılıyor...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni bağlantı: " + clientSocket.getInetAddress());
                addPlayerToLobby(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }

    /**
     * Oyuncuyu lobiye ekler ve 2 kişi olunca yeni oyun odası başlatır.
     * @param playerSocket Bağlanan oyuncu socket'i
     */
    private static void addPlayerToLobby(Socket playerSocket) {
        synchronized (lobby) {
            lobby.add(playerSocket);
            System.out.println("Lobiye oyuncu eklendi: " + playerSocket.getInetAddress());

            if (lobby.size() >= 2) {
                Socket player1 = lobby.poll();
                Socket player2 = lobby.poll();
                GameRoom room = new GameRoom();
                gameRooms.add(room);

                room.addPlayer(player1, "Player 1", "WHITE");
                room.addPlayer(player2, "Player 2", "BLACK");

                System.out.println("Oyun odası oluşturuldu: " + room.getRoomID());

                // Her oyuncuya özel START mesajı gönder
                sendMessage(player1, "START:Player 1:WHITE");
                sendMessage(player2, "START:Player 2:BLACK");

                // Sıra bilgisini herkese yayınla
                broadcastMessage(room, "TURN:Player 1:WHITE");
            }
        }
    }

    /**
     * Belirli oyuncuya mesaj gönderir.
     * @param playerSocket Oyuncu socket'i
     * @param message Gönderilecek mesaj
     */
    private static void sendMessage(Socket playerSocket, String message) {
        try {
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
            out.println(message);
            System.out.println("Mesaj gönderildi (özel): " + message);
        } catch (IOException e) {
            System.err.println("Mesaj gönderme hatası (özel): " + e.getMessage());
        }
    }

    /**
     * Belirli bir odaya mesaj yayını yapar.
     * @param room Oyun odası
     * @param message Yayınlanacak mesaj
     */
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
