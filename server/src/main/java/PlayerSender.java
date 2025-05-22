import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;

public class PlayerSender {

  AudioInputStream in;
  private String route;
  private InetAddress clienteIPAddress;
  private int clientPort;
  private DatagramSocket socket;
  private int BUFFER_SIZE = 1024;


  public PlayerSender(String route, DatagramPacket receivPacket, DatagramSocket serverSocket) {

    this.route = route;
    this.clienteIPAddress = receivPacket.getAddress();
    this.clientPort = receivPacket.getPort();
    this.socket = serverSocket;

  }


  //Metodo principal
  public void sendAudio() {
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE+4);

    try{

      InputStream rawStream = getClass().getResourceAsStream("/songs/Song1_16k.wav");
      BufferedInputStream audioStream = new BufferedInputStream(rawStream);
      in = AudioSystem.getAudioInputStream(audioStream);

      int count = 0;
      while((bytesRead = in.read(buffer, 0, buffer.length)) != -1){

        byteBuffer.clear();
        byteBuffer.putInt(count);
        byteBuffer.put(buffer, 0, bytesRead);
        byte data[] = byteBuffer.array();

        sendPacket(data);

        System.out.println("Sent packet" + count++);
        // count++;

      }
      byteBuffer.clear();
      byteBuffer.putInt(-1);
      byte data[] = byteBuffer.array();
      sendPacket(data);
      socket.close();


    } catch (Exception e){
      e.printStackTrace();
    }
  }



  //Armar paquete y ponerlo en el socket
  public void sendPacket(byte[] audioData) throws Exception {

    DatagramPacket packet = new DatagramPacket(audioData, audioData.length, clienteIPAddress, clientPort);
    socket.send(packet);
  
  }


}