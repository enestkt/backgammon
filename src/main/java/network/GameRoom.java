package network;

import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import logic.GameManager;
import model.Color;
import model.Player;

public class GameRoom {

    private final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>(2));
    private final int roomID;
    private int currentPlayerIndex = 0;
    private GameManager gameManager = null;
    private Player whitePlayer = null;
    private Player blackPlayer = null;
    private String whiteName = null;
    private String blackName = null;

    public GameRoom() {
        this.roomID = this.hashCode();
    }

    // Odaya oyuncu ekleme
    public synchronized void addPlayer(Socket clientSocket, String playerName, String color) {
        ClientHandler clientHandler = new ClientHandler(clientSocket, playerName, color, this);
        players.add(clientHandler);

        // Oyuncu isimlerini ve nesnelerini kaydet
        if (color.equalsIgnoreCase("WHITE")) {
            whiteName = playerName;
            whitePlayer = new Player(playerName, Color.WHITE);
        } else if (color.equalsIgnoreCase("BLACK")) {
            blackName = playerName;
            blackPlayer = new Player(playerName, Color.BLACK);
        }

        // Her iki oyuncu da eklendiyse GameManager'ı oluştur
        if (whiteName != null && blackName != null && gameManager == null) {
            gameManager = new GameManager(whiteName, blackName);
            System.out.println("GameManager sunucuda başlatıldı!");
        }

        broadcast("JOIN:" + playerName + ":" + color);
        System.out.println("Oda ID: " + roomID + " - Oyuncu eklendi: " + playerName + " (" + color + ")");
        new Thread(clientHandler).start();
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

    // Hamle geçerli mi kontrolü (sadece sunucuda)
    public synchronized boolean isMoveValid(String playerName, int from, int to) {
        if (gameManager == null) {
            return false;
        }
        Player player = null;
        if (whitePlayer != null && whitePlayer.getName().equals(playerName)) {
            player = whitePlayer;
        } else if (blackPlayer != null && blackPlayer.getName().equals(playerName)) {
            player = blackPlayer;
        }
        if (player == null) {
            return false;
        }
        if (!getCurrentPlayerName().equals(playerName)) {
            return false; // Sıra kontrolü
        }
        gameManager.setCurrentPlayer(player); // O anki oyuncu olarak güncelle
        // Zar değerine ve tahtaya göre kontrol:
        return gameManager.moveCheckerTestOnly(from, to);
    }

    // Geçerli hamle ise GameManager’da gerçekten uygula
    public synchronized void applyMove(String playerName, int from, int to) {
        if (gameManager == null) {
            return;
        }
        Player player = null;
        if (whitePlayer != null && whitePlayer.getName().equals(playerName)) {
            player = whitePlayer;
        } else if (blackPlayer != null && blackPlayer.getName().equals(playerName)) {
            player = blackPlayer;
        }
        if (player == null) {
            return;
        }
        gameManager.setCurrentPlayer(player);
        boolean success = gameManager.moveChecker(from, to);  // <--- Önemli
        // >>> Eğer success && moveValues.isEmpty() ise, sunucuda sırayı değiştir!
        if (success && gameManager.getRemainingMoves().isEmpty()) {
            switchTurn();
        }
    }

    public synchronized boolean isBarMoveValid(String playerName, int to) {
        if (gameManager == null) {
            return false;
        }
        Player player = playerName.equals(whiteName) ? whitePlayer : blackPlayer;
        if (player == null) {
            return false;
        }
        if (!getCurrentPlayerName().equals(playerName)) {
            return false;
        }
        gameManager.setCurrentPlayer(player);
        // Kontrolü GameManager’da yap
        return gameManager.canEnterFromBarTo(to);
    }

    public synchronized boolean isMoveValuesEmpty() {
        if (gameManager == null) {
            return true;
        }
        return gameManager.getRemainingMoves().isEmpty();
    }

    public synchronized void applyBarMove(String playerName, int to) {
        if (gameManager == null) {
            return;
        }
        Player player = playerName.equals(whiteName) ? whitePlayer : blackPlayer;
        if (player == null) {
            return;
        }
        gameManager.setCurrentPlayer(player);
        gameManager.moveFromBarServer(to);
    }

    // Zar değerlerini GameManager'a gönder
    public synchronized void setDiceValues(int die1, int die2) {
        if (gameManager != null) {
            gameManager.setDiceValues(die1, die2);
        } else {
            System.err.println("HATA: GameManager henüz oluşturulmadı! Zar güncellenemedi.");
        }
    }

    // Sıra değiştirme
    public synchronized void switchTurn() {
        currentPlayerIndex = (currentPlayerIndex == 1) ? 0 : 1;
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

    public synchronized List<ClientHandler> getClientHandlers() {
        return new ArrayList<>(players);
    }

}
