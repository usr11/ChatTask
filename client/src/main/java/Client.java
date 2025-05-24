import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import javax.sound.sampled.*;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // o la IP del servidor
        int port = 6789;
        int voicePort = 5000;

        try (
            Socket socket = new Socket(serverAddress, port);
            Socket voiceSocket = new Socket(serverAddress, voicePort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            Scanner scanner = new Scanner(System.in);

            // Hilo para leer mensajes del servidor
            new Thread(new Lector(in)).start();

            // Flujo de audio (voz)
            new Thread(new VoiceSender(voiceSocket)).start();

            while (true) {
                System.out.println("[1] Para mensaje - [2] Para audio - [3] Para salir");
                String opcion = scanner.nextLine();

                switch (opcion) {
                    case "1":
                        String message = scanner.nextLine();
                        out.println(message);
                        break;
                    case "2":
                        // Audio ya se está enviando continuamente en otro hilo (opcional)
                        System.out.println("Enviando audio...");
                        break;
                    case "3":
                        socket.close();
                        voiceSocket.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opción no válida");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
