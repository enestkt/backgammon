package network;

import java.io.*;
import java.net.*;

public class Client {

    private String serverIp;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String serverIp, int serverPort) throws IOException {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        connectToServer();
    }

    private void connectToServer() throws IOException {
        socket = new Socket(serverIp, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Sunucuya bağlandı: " + serverIp);
        listenForMessages();
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Sunucudan gelen: " + message);
                }
            } catch (IOException e) {
                System.err.println("Bağlantı hatası: " + e.getMessage());
            }
        }).start();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Bağlantı kapatma hatası: " + e.getMessage());
        }
    }
}
