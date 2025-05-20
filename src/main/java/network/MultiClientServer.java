// MultiClientServer.java - Geliştirilmiş Sürüm
package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

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
                addPlayerToLobby(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }

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

                new Thread(() -> handleClient(player1, room)).start();
                new Thread(() -> handleClient(player2, room)).start();
            }
        }
    }

    private static void sendMessage(Socket playerSocket, String message) {
        try {
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
            out.println(message);
            System.out.println("Mesaj gönderildi (özel): " + message);
        } catch (IOException e) {
            System.err.println("Mesaj gönderme hatası (özel): " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket, GameRoom assignedRoom) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Sunucuya gelen mesaj: " + message);
                if (message.startsWith("MOVE:") || message.startsWith("ROLL:")) {
                    String currentPlayer = assignedRoom.getCurrentPlayerName();
                    String playerName = message.split(":")[1];
                    if (!currentPlayer.equals(playerName)) {
                        System.out.println("Sıra hatası: " + playerName);
                        out.println("ERROR:Sıra " + currentPlayer + "'da!");
                        continue;
                    }
                    assignedRoom.switchTurn();
                }
                broadcastMessage(assignedRoom, message);
            }
        } catch (IOException e) {
            System.err.println("İstemci bağlantı hatası: " + e.getMessage());
        } finally {
            System.out.println("Oyuncu bağlantısı kesildi.");
            assignedRoom.removePlayer(new ClientHandler(clientSocket, "", "", assignedRoom));
            broadcastMessage(assignedRoom, "LEFT:" + clientSocket.getInetAddress());
        }
    }

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
