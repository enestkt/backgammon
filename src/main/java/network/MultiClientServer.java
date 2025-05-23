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
        String myPlayerName = null;

        // Oyuncunun adı hangi sokete atanmışsa o
        // Bunu addPlayerToLobby'de verdiğin "Player 1"/"Player 2" ile mapleyebilirsin
        // Veya GameRoom üzerinden çekebilirsin
        // Bağlanan oyuncunun adını bul
        for (ClientHandler handler : assignedRoom.getClientHandlers()) {
            if (handler.getSocket().equals(clientSocket)) {
                myPlayerName = handler.getPlayerName();
                break;
            }
        }

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Sunucuya gelen mesaj: " + message);

                if (message.startsWith("MOVE:")) {
                    // Ör: "MOVE:Player 1:from:to"
                    String[] parts = message.split(":");
                    if (parts.length != 4) {
                        continue;
                    }
                    String movePlayer = parts[1];
                    if (!assignedRoom.getCurrentPlayerName().equals(movePlayer)) {
                        System.out.println("Sıra hatası: " + movePlayer);
                        out.println("ERROR:Sıra " + assignedRoom.getCurrentPlayerName() + "'da!");
                        continue;
                    }
                    // Hamle geçerli mi kontrolü sunucu tarafında yapılmalı!
                    if (assignedRoom.isMoveValid(movePlayer, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]))) {
                        assignedRoom.applyMove(movePlayer, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                        broadcastMessage(assignedRoom, message);
                        assignedRoom.switchTurn();
                    } else {
                        out.println("ERROR:Geçersiz hamle! Zar veya taş kuralına uymuyor.");
                    }
                    continue;
                }

                if (message.startsWith("ROLL:")) {
                    // Oyuncu zar atmak istiyor
                    if (!assignedRoom.getCurrentPlayerName().equals(myPlayerName)) {
                        out.println("ERROR:Sıra sende değil!");
                        continue;
                    }
                    // Zarları sunucu atsın!
                    int die1 = (int) (Math.random() * 6) + 1;
                    int die2 = (int) (Math.random() * 6) + 1;
                    assignedRoom.setDiceValues(die1, die2);
                    broadcastMessage(assignedRoom, "ROLL:" + myPlayerName + ":" + die1 + ":" + die2);
                    System.out.println("🎲 Zar atıldı: " + myPlayerName + " - " + die1 + ", " + die2);
                    continue;
                }

                if (message.startsWith("SWITCH_TURN:")) {
                    assignedRoom.switchTurn();
                    continue;
                }

                // Diğer tüm mesajları broadcast et (örn. CHAT, LEFT)
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
