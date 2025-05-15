package network;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameRoom {
    private final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>(2));

    public synchronized void addPlayer(Socket clientSocket) {
        String playerName = "Player " + (players.size() + 1);
        ClientHandler clientHandler = new ClientHandler(clientSocket, playerName);
        players.add(clientHandler);
        new Thread(clientHandler).start();
        broadcast("JOIN:" + playerName);  // Oyuncunun katıldığını yayınla
    }

    public synchronized boolean isFull() {
        return players.size() >= 2;
    }

    private synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private final String playerName;

        public ClientHandler(Socket socket, String playerName) {
            this.socket = socket;
            this.playerName = playerName;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                broadcast("JOIN:" + playerName);  // Katılım bilgisini yayınla
                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(playerName + ": " + message);
                }
            } catch (IOException e) {
                System.err.println(playerName + " bağlantı hatası: " + e.getMessage());
            } finally {
                players.remove(this);
                broadcast("LEFT:" + playerName);
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Socket kapatma hatası: " + e.getMessage());
                }
            }
        }

        private void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
