package network;

import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import logic.GameManager;
import model.Color;
import model.Player;

/**
 * GameRoom sınıfı, bir oyun odasını ve odaya ait oyuncuları yönetir.
 * Her oda 2 oyuncudan oluşur, sunucudaki oyunun ilerleyişi ve kuralların merkezi burada sağlanır.
 */
public class GameRoom {

    // Odaya bağlı oyuncuları (ClientHandler) tutar
    private final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>(2));

    // Her odanın benzersiz ID'si
    private final int roomID;

    // Şu anki oyuncu dizinini tutar (0 veya 1)
    private int currentPlayerIndex = 0;

    // Oyun mantığı ve akışını yöneten ana sınıf
    private GameManager gameManager = null;

    // Her iki oyuncunun nesneleri ve adları
    private Player whitePlayer = null;
    private Player blackPlayer = null;
    private String whiteName = null;
    private String blackName = null;

    /**
     * GameRoom yapıcı metodu. Oda ID'si atanır.
     */
    public GameRoom() {
        this.roomID = this.hashCode();
    }

    /**
     * Odaya yeni oyuncu ekler, gerekli atamaları yapar ve GameManager başlatır.
     * @param clientSocket Oyuncunun bağlantı soketi
     * @param playerName Oyuncu adı
     * @param color Oyuncu rengi
     */
    public synchronized void addPlayer(Socket clientSocket, String playerName, String color) {
        ClientHandler clientHandler = new ClientHandler(clientSocket, playerName, color, this);
        players.add(clientHandler);

        // Oyuncu nesnesini ve adını uygun şekilde kaydet
        if (color.equalsIgnoreCase("WHITE")) {
            whiteName = playerName;
            whitePlayer = new Player(playerName, Color.WHITE);
        } else if (color.equalsIgnoreCase("BLACK")) {
            blackName = playerName;
            blackPlayer = new Player(playerName, Color.BLACK);
        }

        // İki oyuncu da geldiyse GameManager başlat
        if (whiteName != null && blackName != null && gameManager == null) {
            gameManager = new GameManager(whiteName, blackName);
            System.out.println("GameManager sunucuda başlatıldı!");
        }

        broadcast("JOIN:" + playerName + ":" + color);
        System.out.println("Oda ID: " + roomID + " - Oyuncu eklendi: " + playerName + " (" + color + ")");
        new Thread(clientHandler).start();
    }

    /**
     * Odaya bağlı bir oyuncuyu çıkarır ve oyunculara bildirir.
     * @param player Çıkarılacak oyuncunun ClientHandler'ı
     */
    public synchronized void removePlayer(ClientHandler player) {
        players.remove(player);
        broadcast("LEFT:" + player.getPlayerName());
        System.out.println("Oyuncu ayrıldı: " + player.getPlayerName());
    }

    /**
     * Oda içindeki tüm oyunculara mesaj gönderir.
     * @param message Gönderilecek mesaj
     */
    public synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    /**
     * Bir hamlenin sunucu kurallarına göre geçerli olup olmadığını kontrol eder.
     * @param playerName Hamle yapan oyuncunun adı
     * @param from Taşın geldiği index
     * @param to Taşın gideceği index
     * @return Geçerli ise true
     */
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
        return gameManager.moveCheckerTestOnly(from, to);
    }

    /**
     * Geçerli hamleyi sunucu tarafında uygular ve sırayı yönetir.
     * @param playerName Hamle yapan oyuncunun adı
     * @param from Başlangıç noktası
     * @param to Bitiş noktası
     */
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
        boolean success = gameManager.moveChecker(from, to);
        if (success && gameManager.getRemainingMoves().isEmpty()) {
            switchTurn();
        }
    }

    /**
     * Bar'dan giriş hamlesi geçerli mi kontrol eder.
     * @param playerName Oyuncunun adı
     * @param to Bar'dan girdiği nokta
     * @return Geçerli ise true
     */
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
        return gameManager.canEnterFromBarTo(to);
    }

    /**
     * O anki oyuncunun kalan hamlesi yok mu kontrolü.
     * @return Kalan hamle yoksa true
     */
    public synchronized boolean isMoveValuesEmpty() {
        if (gameManager == null) {
            return true;
        }
        return gameManager.getRemainingMoves().isEmpty();
    }

    /**
     * Bar'dan giriş hamlesini uygular.
     * @param playerName Oyuncunun adı
     * @param to Bar'dan girdiği nokta
     */
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

    /**
     * Zar değerlerini günceller.
     * @param die1 Birinci zar
     * @param die2 İkinci zar
     */
    public synchronized void setDiceValues(int die1, int die2) {
        if (gameManager != null) {
            gameManager.setDiceValues(die1, die2);
        } else {
            System.err.println("HATA: GameManager henüz oluşturulmadı! Zar güncellenemedi.");
        }
    }

    /**
     * Sırayı değiştirir (hamle hakkını diğer oyuncuya verir) ve oyunculara bildirir.
     */
    public synchronized void switchTurn() {
        currentPlayerIndex = (currentPlayerIndex == 1) ? 0 : 1;
        String nextPlayer = players.get(currentPlayerIndex).getPlayerName();
        broadcast("TURN:" + nextPlayer);
        System.out.println("Sıra değişti: " + nextPlayer);
    }

    /**
     * Şu anki oyuncunun adını döndürür.
     * @return Mevcut oyuncunun adı
     */
    public String getCurrentPlayerName() {
        return players.get(currentPlayerIndex).getPlayerName();
    }

    /**
     * Oda ID'sini döndürür.
     * @return roomID
     */
    public int getRoomID() {
        return this.roomID;
    }

    /**
     * Odaya bağlı oyuncuların bağlantılarını (socket) döndürür.
     * @return Socket listesi
     */
    public synchronized List<Socket> getPlayers() {
        List<Socket> sockets = new ArrayList<>();
        for (ClientHandler player : players) {
            sockets.add(player.getSocket());
        }
        return sockets;
    }

    /**
     * Odaya bağlı tüm ClientHandler nesnelerini döndürür.
     * @return ClientHandler listesi
     */
    public synchronized List<ClientHandler> getClientHandlers() {
        return new ArrayList<>(players);
    }
}
