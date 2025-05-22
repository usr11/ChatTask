import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Client {
    private static final String SERVER_IP = "172.30.168.216";
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
                //repetir el ciclo hasta que no ingrese un nombre valido
                if (message.startsWith("SUBMITNAME")) {
                    System.out.print("Ingrese nombre de usuario: ");
                    String name = userInput.readLine();
                    out.println(name);
                }
                else if (message.startsWith("NAMEACCEPTED")) {
                    System.out.println("Nombre aceptado!!");
                    break;}
            }
                   
            //creamos el objeto lector e iniciamos el hilo
            Lector lector = new Lector(in);
            new Thread(lector).start();

            
            int option = 0;
            System.out.println("[1] Para mensaje - [2] Para audio - [3] Para salir");
            
            while(option != 3) {

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

    public static void textMessage(){
      try {
        //estar atento a la entrada del usuario          
        System.out.println("Ingrese el mensaje a enviar (@nombre para enviar por privado)");
        if ((message = userInput.readLine()) != null) {
            System.out.println("Ingrese el mensaje (@nombre para enviar por privado)");
            out.println(message);
        }

      } catch (IOException e){
        System.out.println("Error al leer el menasaje:" + e.getMessage());
      }
    }


    public static void audioMessage(byte[] audioMessage) throws Exception {

        InetAddress IPAddress = InetAddress.getByName("localhost");
        int PORT = 1205;
        int BUFFER_SIZE = 1024 + 4;  // 4 bytes para número de paquete
        DatagramSocket clientSocket = new DatagramSocket();

        // Inicializar el hilo reproductor
        AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
        PlayerThread playerThread = new PlayerThread(audioFormat, BUFFER_SIZE);
        playerThread.start();

        // Enviar paquetes al servidor
        int totalPackets = (int) Math.ceil((double) audioMessage.length / 1024);
        for (int i = 0; i < totalPackets; i++) {
            int start = i * 1024;
            int length = Math.min(1024, audioMessage.length - start);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.putInt(i); // número de paquete
            buffer.put(audioMessage, start, length);

            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.position(), IPAddress, PORT);
            clientSocket.send(packet);
        }

        // Enviar paquete final con número de paquete = -1
        ByteBuffer endBuffer = ByteBuffer.allocate(4);
        endBuffer.putInt(-1);
        DatagramPacket endPacket = new DatagramPacket(endBuffer.array(), endBuffer.position(), IPAddress, PORT);
        clientSocket.send(endPacket);

        // Esperar respuesta del servidor y reproducirla
        byte[] receiveBuffer = new byte[BUFFER_SIZE];
        while (true) {
            DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            clientSocket.receive(responsePacket);

            ByteBuffer byteBuffer = ByteBuffer.wrap(responsePacket.getData(), 0, responsePacket.getLength());
            int packetCount = byteBuffer.getInt();

            if (packetCount == -1) {
                System.out.println("Cliente: audio final recibido.");
                break;
            } else {
                byte[] data = new byte[responsePacket.getLength() - 4];
                byteBuffer.get(data, 0, data.length);
                playerThread.addBytes(data);
            }
        }

        clientSocket.close();
    }



    public static void captureSound() throws Exception {
      AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("El micrófono no es compatible.");
            return;
        }

        try {
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            System.out.println("Capturando audio por 5 segundo");

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream outMic = new ByteArrayOutputStream();

            // Captura por 5 segundos
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5000) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                outMic.write(buffer, 0, bytesRead);
            }

            microphone.stop();
            microphone.close();
            byte[] audioBytes = outMic.toByteArray();

            System.out.println("Audio capturado de " + audioBytes.length);

            audioMessage(audioBytes);

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }


}