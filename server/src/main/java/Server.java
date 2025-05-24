import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;

public class Server {

    private static Set<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) {

        int PORT = 6789;
        Chatters clientes = new Chatters();

        try {
            // TCP PARA MENSAJES
            ServerSocket serverSocketTCP = new ServerSocket(PORT);
            System.out.println("Servidor iniciado. Esperando clientes...(Mensajes) TCP");

            // UDP PARA AUDIOS
            new Thread(() -> {
                try {
                    AudioSender audioSender = new AudioSender();
                    audioSender.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // ✅ LLAMADAS DE VOZ EN TIEMPO REAL (TCP)
            new Thread(() -> {
                try {
                    ServerSocket voiceSocket = new ServerSocket(5000);
                    System.out.println("Servidor de llamadas iniciado en el puerto 5000");
                    while (true) {
                        Socket voiceClient = voiceSocket.accept();
                        System.out.println("Cliente conectado a llamadas: " + voiceClient.getInetAddress());
                        new Thread(new VoiceCallHandler(voiceClient)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            // ⬆️ FIN DEL BLOQUE DE LLAMADAS DE VOZ

            // TCP PARA MENSAJES ENTRANTES
            while (true) {
                Socket clientSocket = serverSocketTCP.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientes);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
