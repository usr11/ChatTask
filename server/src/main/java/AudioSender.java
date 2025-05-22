import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.*;


public class AudioSender {
  
  public static void start() throws Exception{

    InetAddress IPAddress = InetAddress.getByName("localhost");
    String song = "song/Song1_16k.wav";
    int PORT = 1205;

    DatagramSocket serverSocket = new DatagramSocket(PORT, IPAddress);
    
    System.out.println("Servidor iniciado. Esperando solicitud del (Audios) UDP");

    byte[] receiveData = new byte[1024];
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    serverSocket.receive(receivePacket);

    System.out.println("Cliente conectado... Enviando audio");

    //Iniciar envio
    PlayerSender sender = new PlayerSender(song,receivePacket,serverSocket);
    sender.sendAudio();

  }

}
