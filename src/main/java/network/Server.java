package network;

import model.Board;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server: Client'tan bağlantı kabul eder, gelen Board nesnesini alır ve geri gönderir.
 */
public class Server {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Sunucu başlatıldı. Bağlantı bekleniyor...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Bağlantı kabul edildi: " + clientSocket.getInetAddress());

            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

            // Client'tan board al
            Board receivedBoard = (Board) input.readObject();
            System.out.println("Board alındı ve geri gönderiliyor...");

            // Gelen board'ı aynen geri yolla (test amaçlı)
            output.writeObject(receivedBoard);

            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
