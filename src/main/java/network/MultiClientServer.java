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
                }
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }
}
