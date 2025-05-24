import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.*;


public class AudioSender {
  
public static void start() throws Exception {
        InetAddress IPAddress = InetAddress.getByName("172.20.10.3");
        int PORT = 1205;
        int BUFFER_SIZE = 1024 + 4;  // 4 bytes para número de paquete
        DatagramSocket serverSocket = new DatagramSocket(PORT, IPAddress);

        System.out.println("Servidor iniciado. Esperando solicitud del cliente (Audio UDP)");

        ByteArrayOutputStream audioDataStream = new ByteArrayOutputStream();
        byte[] receiveBuffer = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        InetAddress clientAddress = null;
        int clientPort = -1;

        // Recibir paquetes hasta encontrar uno con número de secuencia -1
        while (true) {
            serverSocket.receive(receivePacket);

            ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData(), 0, receivePacket.getLength());
            int packetNumber = byteBuffer.getInt();

            if (packetNumber == -1) {
                System.out.println("Fin de audio recibido");
                break;
            }

            byte[] audioPart = new byte[receivePacket.getLength() - 4];
            byteBuffer.get(audioPart);
            audioDataStream.write(audioPart);

            // Guardar IP y puerto del cliente para enviar de vuelta
            clientAddress = receivePacket.getAddress();
            clientPort = receivePacket.getPort();
        }

        // Ahora reenviar el audio recibido
        byte[] audioBytes = audioDataStream.toByteArray();
        PlayerSender sender = new PlayerSender(audioBytes, clientAddress, clientPort, serverSocket);
        sender.sendAudio();
    }
}
