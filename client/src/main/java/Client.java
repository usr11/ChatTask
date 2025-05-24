import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Client {
  private static final String SERVER_IP = "192.168.1.4";
  private static final int PORT = 6789;

  static Socket socket;
  static PrintWriter out;
  static BufferedReader in;
  static BufferedReader userInput;
  static String message;

  public static void main(String[] args) throws Exception {
    try {
      socket = new Socket(SERVER_IP, PORT);
      System.out.println("Conectado al servidor.");

      userInput = new BufferedReader(new InputStreamReader(System.in));
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      while ((message = in.readLine()) != null) {
        if (message.startsWith("SUBMITNAME")) {
          System.out.print("Ingrese nombre de usuario: ");
          String name = userInput.readLine();
          out.println(name);
        } else if (message.startsWith("NAMEACCEPTED")) {
          System.out.println("Nombre aceptado!!");
          break;
        }
      }

      Lector lector = new Lector(in);
      new Thread(lector).start();

      int option = 0;
      System.out.println("[1] Para mensaje - [2] Para audio - [3] Para salir - [4] Para salir");

      while (option != 3) {
        String optionInput = userInput.readLine();
        option = Integer.parseInt(optionInput);

        switch (option) {
          case 1:
            textMessage();
            break;
          case 2:
            captureSound();
            break;
          case 3:
          
            break;
          case 4:
            System.out.println("Hasta pronto...");
            break;
          default:
            System.out.println("Opcion no valida");
            break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void textMessage() {
    try {
      System.out.println("Ingrese el mensaje a enviar (@nombre para enviar por privado)");
      if ((message = userInput.readLine()) != null) {
        out.println(message);
      }
    } catch (IOException e) {
      System.out.println("Error al leer el mensaje: " + e.getMessage());
    }
  }

  public static void audioMessage(byte[] audioMessage) throws Exception {
    InetAddress IPAddress = InetAddress.getByName("192.168.1.4");
    int PORT = 1205;
    int BUFFER_SIZE = 1024 + 4;
    DatagramSocket clientSocket = new DatagramSocket();
    PlayerThread playerThread;

    AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
    playerThread = new PlayerThread(audioFormat, BUFFER_SIZE);
    playerThread.start();

    int totalPackets = (int) Math.ceil((double) audioMessage.length / 1024);
    for (int i = 0; i < totalPackets; i++) {
      int start = i * 1024;
      int length = Math.min(1024, audioMessage.length - start);

      ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
      buffer.putInt(i);
      buffer.put(audioMessage, start, length);

      DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.position(), IPAddress, PORT);
      clientSocket.send(packet);
    }

    // byte[] buffer = new byte[BUFFER_SIZE];
    // int count = 0;

    // while(true){

    // DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    // clientSocket.receive(packet);

    // buffer = packet.getData();
    // ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
    // int packetCount = byteBuffer.getInt();

    // if(packetCount == -1){
    // System.out.println("Received last packet " + count);
    // break;

    // } else {

    // byte[] data = new byte[1024];
    // byteBuffer.get(data, 0, data.length);
    // playerThread.addBytes(data);
    // System.out.println("Received packet " + packetCount + " current: " + count);

    // }

    // count++;

    // }

    // clientSocket.close();

    ByteBuffer endBuffer = ByteBuffer.allocate(4);
    endBuffer.putInt(-1);
    DatagramPacket endPacket = new DatagramPacket(endBuffer.array(), endBuffer.position(), IPAddress, PORT);
    clientSocket.send(endPacket);

    byte[] receiveBuffer = new byte[BUFFER_SIZE];
    int count = 0;
    while (true) {
      DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
      clientSocket.receive(responsePacket);

      ByteBuffer byteBuffer = ByteBuffer.wrap(responsePacket.getData(), 0, responsePacket.getLength());
      int packetCount = byteBuffer.getInt();

      if (packetCount == -1) {
        //System.out.println("Cliente: audio final recibido.");
        break;
      } else {
        byte[] data = new byte[responsePacket.getLength() - 4];
        byteBuffer.get(data, 0, data.length);
        playerThread.addBytes(data);
        //System.out.println("Cliente: paquete recibido " + packetCount + " (" + count + ")");
      }
      count++;
    }

    clientSocket.close();
  }

  public static void captureSound() throws Exception {
    AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

    if (!AudioSystem.isLineSupported(info)) {
      System.out.println("El micrófono no es compatible.");
      return;
    }

    TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
    microphone.open(format);
    microphone.start();

    System.out.println("Capturando audio... Presiona ENTER para detener.");

    byte[] buffer = new byte[4096];
    ByteArrayOutputStream outMic = new ByteArrayOutputStream();

    Thread captureThread = new Thread(() -> {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          int bytesRead = microphone.read(buffer, 0, buffer.length);
          outMic.write(buffer, 0, bytesRead);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    long startTime = System.currentTimeMillis();
    captureThread.start();

    // Esperar que el usuario presione Enter
    new Scanner(System.in).nextLine();

    long endTime = System.currentTimeMillis();
    long durationInSeconds = (endTime - startTime) / 1000;

    captureThread.interrupt();
    microphone.stop();
    microphone.close();

    byte[] audioBytes = outMic.toByteArray();
    System.out.println("Audio capturado.");
    System.out.println("Duración: " + durationInSeconds + " segundos.");

    audioMessage(audioBytes);
  }
}
