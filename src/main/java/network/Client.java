package network;

import model.Board;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Client: Sunucuya bağlanıp Board nesnesini gönderir.
 */
public class Client {

    private String serverIp;
    private int serverPort;

    public Client(String serverIp, int serverPort) throws IOException {
    this.serverIp = serverIp;
    this.serverPort = serverPort;
}


    public void sendBoard(Board board) {
        try (Socket socket = new Socket(serverIp, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(board);
            out.flush();
            System.out.println("Tahta sunucuya başarıyla gönderildi.");

        } catch (IOException e) {
            System.err.println("Sunucuya bağlanırken hata oluştu: " + e.getMessage());
        }
    }
}
