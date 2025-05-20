// GameRoom.java
package network;

import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class GameRoom {

    private final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>(2));
    private final int roomID;
    private int currentPlayerIndex = 0;

    public GameRoom() {
        this.roomID = this.hashCode();
    }

    // Odaya oyuncu ekleme
    public synchronized void addPlayer(Socket clientSocket, String playerName, String color) {
        ClientHandler clientHandler = new ClientHandler(clientSocket, playerName, color, this);
        players.add(clientHandler);
        new Thread(clientHandler).start();
        broadcast("JOIN:" + playerName + ":" + color);
        System.out.println("Oda ID: " + roomID + " - Oyuncu eklendi: " + playerName + " (" + color + ")");
    }

    // Oyuncu kaldırma
    public synchronized void removePlayer(ClientHandler player) {
        players.remove(player);
        broadcast("LEFT:" + player.getPlayerName());
        System.out.println("Oyuncu ayrıldı: " + player.getPlayerName());
    }

    // Oyunculara mesaj gönderme
    public synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    // Sıra değiştirme
    public synchronized void switchTurn() {
        if (currentPlayerIndex == 1){
            currentPlayerIndex= 0;
                    
        }else{
            currentPlayerIndex = 1;
        }

        String nextPlayer = players.get(currentPlayerIndex).getPlayerName();
        broadcast("TURN:" + nextPlayer);
        System.out.println("Sıra değişti: " + nextPlayer);
    }

    // Mevcut oyuncu adını döndürme
    public String getCurrentPlayerName() {
        return players.get(currentPlayerIndex).getPlayerName();
    }

    // Oda ID'sini döndürme
    public int getRoomID() {
        return this.roomID;
    }

    // Oyuncu listesini döndürme
    public synchronized List<Socket> getPlayers() {
        List<Socket> sockets = new ArrayList<>();
        for (ClientHandler player : players) {
            sockets.add(player.getSocket());
        }
        return sockets;
    }
}
