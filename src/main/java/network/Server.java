package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        int port = 5000;
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Yerel Sunucu başlatıldı. Port: " + port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Baglanti saglandi: " + clientSocket.getInetAddress());

                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                    String message = (String) input.readObject();
                    System.out.println("Mesaj alındı: " + message);

                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                    output.writeObject("Sunucu: Mesaj alındı - " + message);
                    output.flush();
                    System.out.println("Yanıt gönderildi.");

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Veri işleme hatası: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatma hatası: " + e.getMessage());
        }
    }
}
