package network;

import java.io.*;
import java.net.*;
import java.util.*;

public class MultiClientServer {

    private static final int PORT = 5000;
    private static final List<GameRoom> gameRooms = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("Çoklu Oyuncu Sunucusu başlatılıyor...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni bağlantı: " + clientSocket.getInetAddress());

                // Odaya oyuncu ekleme
                GameRoom assignedRoom = null;

                synchronized (gameRooms) {
                    for (GameRoom room : gameRooms) {
                        if (!room.isFull()) {
                            assignedRoom = room;
                            break;
                        }
                    }

                    if (assignedRoom == null) {
                        assignedRoom = new GameRoom();
                        gameRooms.add(assignedRoom);
                        System.out.println("Yeni oyun odası oluşturuldu. Toplam Odalar: " + gameRooms.size());
                    }

                    assignedRoom.addPlayer(clientSocket);
                    System.out.println("Oyuncu odaya eklendi: " + clientSocket.getInetAddress());
                    System.out.println("Oda ID: " + assignedRoom.getRoomID() + " - Oyuncu Sayısı: " + assignedRoom.getPlayerCount());

                    // Tüm oyunculara katılım mesajı gönder
                    broadcastMessage(assignedRoom, "JOIN:Oyuncu katıldı: " + clientSocket.getInetAddress());
                }

                final GameRoom room = assignedRoom;
                new Thread(() -> handleClient(clientSocket, room)).start();
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket, GameRoom assignedRoom) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Sunucuya gelen mesaj: " + message);

                if (message.startsWith("ROLL:") || message.startsWith("MOVE:") || message.startsWith("CHAT:") || message.startsWith("LEFT:")) {
                    broadcastMessage(assignedRoom, message);
                } else {
                    System.err.println("Geçersiz mesaj formatı: " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("İstemci bağlantı hatası: " + e.getMessage());
        }
    }

    private static void broadcastMessage(GameRoom room, String message) {
        synchronized (room) {
            for (Socket playerSocket : room.getPlayers()) {
                try {
                    PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
                    System.err.println("Mesaj gönderme hatası: " + e.getMessage());
                }
            }
        }
    }
}
