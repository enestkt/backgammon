package network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MultiClientServer {
    private static final int PORT = 5000;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static int playerCount = 1;

    public static void main(String[] args) {
        System.out.println("Çoklu Oyuncu Sunucusu başlatılıyor...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String playerName = "Player " + playerCount;
                playerCount++;
                System.out.println(playerName + " bağlandı: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, playerName);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final String playerName;
        private PrintWriter out;

        ClientHandler(Socket socket, String playerName) {
            this.socket = socket;
            this.playerName = playerName;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = out;
                broadcast(playerName + " katıldı!");
                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(playerName + ": " + message);
                }
            } catch (IOException e) {
                System.err.println("Bağlantı kesildi: " + playerName);
            } finally {
                clients.remove(this);
                broadcast(playerName + " ayrıldı.");
                try { socket.close(); } catch (IOException e) { }
            }
        }

        void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }

        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
