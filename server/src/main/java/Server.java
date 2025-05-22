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
            //TCP PARA MENSAJES
            ServerSocket serverSocketTCP = new ServerSocket(PORT);
            System.out.println("Servidor iniciado. Esperando clientes...(Mensajes) TCP");
            
            //UDP PARA AUDIOS
            new Thread(() -> {
                try {

                    AudioSender audioSender = new AudioSender();
                    audioSender.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                Socket clientSocket = serverSocketTCP.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket);
                //crea el objeto Runable
                ClientHandler clientHandler = new ClientHandler(clientSocket,clientes);
                //inicia el hilo con el objeto Runnable
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



   
}

