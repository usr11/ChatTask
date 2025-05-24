import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;

public class PlayerSender {

    private byte[] audioData;
    private InetAddress clienteIPAddress;
    private int clientPort;
    private DatagramSocket socket;
    private final int BUFFER_SIZE = 1024;

    public PlayerSender(byte[] audioData, InetAddress clienteIPAddress, int clientPort, DatagramSocket socket) {
        this.audioData = audioData;
        this.clienteIPAddress = clienteIPAddress;
        this.clientPort = clientPort;
        this.socket = socket;
    }


  //Metodo principal
public void sendAudio() {
        int totalPackets = (int) Math.ceil((double) audioData.length / 1024);
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE + 4);

        try {
            for (int i = 0; i < totalPackets; i++) {
                int start = i * 1024;
                int length = Math.min(1024, audioData.length - start);

                byteBuffer.clear();
                byteBuffer.putInt(i);
                byteBuffer.put(audioData, start, length);
                sendPacket(byteBuffer.array());

                System.out.println("Sent packet " + i);
            }

            // Enviar último paquete de cierre
            byteBuffer.clear();
            byteBuffer.putInt(-1);
            sendPacket(byteBuffer.array());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(byte[] audioData) throws Exception {
        DatagramPacket packet = new DatagramPacket(audioData, audioData.length, clienteIPAddress, clientPort);
        socket.send(packet);
    }


}