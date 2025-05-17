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
                    // Mevcut bir dolmamış odaya oyuncuyu ekleme
                    for (GameRoom room : gameRooms) {
                        if (!room.isFull()) {
                            assignedRoom = room;
                            break;
                        }
                    }

                    // Eğer dolmamış oda yoksa yeni bir oda oluştur
                    if (assignedRoom == null) {
                        assignedRoom = new GameRoom();
                        gameRooms.add(assignedRoom);
                        System.out.println("Yeni oyun odası oluşturuldu. Toplam Odalar: " + gameRooms.size());
                    }

                    // Oyuncuyu odaya ekle
                    assignedRoom.addPlayer(clientSocket);
                    System.out.println("Oyuncu odaya eklendi: " + clientSocket.getInetAddress());

                    // Oda bilgisi ve oyuncu sayısını yazdır
                    System.out.println("Oda ID: " + assignedRoom.getRoomID() + " - Oyuncu Sayısı: " + assignedRoom.getPlayerCount());

                    // Oyuncu katılımını yayınla
                    broadcastMessage(assignedRoom, "JOIN:Oyuncu katıldı: " + clientSocket.getInetAddress(), clientSocket);
                }

                // Mesajları dinleme ve yayma
                final GameRoom room = assignedRoom;
                new Thread(() -> handleClient(clientSocket, room)).start();
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }

    // İstemciden gelen mesajı işleme
    private static void handleClient(Socket clientSocket, GameRoom assignedRoom) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Sunucuya gelen mesaj: " + message);

                // Mesaj türüne göre işlem yap
                if (message.startsWith("ROLL:")) {
                    broadcastMessage(assignedRoom, "ROLL:" + message.substring(5), clientSocket);
                } else if (message.startsWith("MOVE:")) {
                    broadcastMessage(assignedRoom, "MOVE:" + message.substring(5), clientSocket);
                } else if (message.startsWith("CHAT:")) {
                    broadcastMessage(assignedRoom, "CHAT:" + message.substring(5), clientSocket);
                } else if (message.startsWith("LEFT:")) {
                    broadcastMessage(assignedRoom, "LEFT:" + clientSocket.getInetAddress(), clientSocket);
                    System.out.println("Oyuncu ayrıldı: " + clientSocket.getInetAddress());
                } else {
                    broadcastMessage(assignedRoom, "MESAJ:" + message, clientSocket);
                }
            }
        } catch (IOException e) {
            System.err.println("İstemci bağlantı hatası: " + e.getMessage());
        }
    }

    // Mesajı tüm oyunculara yayma metodu
    private static void broadcastMessage(GameRoom room, String message, Socket senderSocket) {
        synchronized (room) {
            for (Socket playerSocket : room.getPlayers()) {
                if (!playerSocket.equals(senderSocket)) {
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
}
