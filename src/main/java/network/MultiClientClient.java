package network;

import java.io.*;
import java.net.*;

public class MultiClientClient {
    private String serverIp;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MultiClientClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        try {
            socket = new Socket(serverIp, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Bağlandı: " + serverIp + ":" + serverPort);
        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
        }
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
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

    public String getServerIp() {
        return serverIp;
    }
}
