package network;

import java.io.*;
import java.net.*;

public class MultiClientClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MultiClientClient(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Bağlandı: " + serverIp + ":" + serverPort);
            listenForMessages();
        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Sunucudan gelen: " + message);
                }
            } catch (IOException e) {
                System.err.println("Sunucudan gelen mesaj alınamadı: " + e.getMessage());
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
                System.out.println("Bağlantı kapatıldı.");
            }
        } catch (IOException e) {
            System.err.println("Bağlantı kapatma hatası: " + e.getMessage());
        }
    }
}
