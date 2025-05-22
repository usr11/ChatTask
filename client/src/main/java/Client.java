import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

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
                  audioMessage();
                  
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


    public static void audioMessage() throws Exception{


      InetAddress IPAddress = InetAddress.getByName("localhost");
      int PORT = 1205;
      int BUFFER_SIZE = 1024 + 4;  
      DatagramSocket clientSocket = new DatagramSocket();
      PlayerThread playerThread;

      AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);

      playerThread = new PlayerThread(audioFormat,BUFFER_SIZE);
      playerThread.start();


      String mensaje = "Hola servidor, enviame una cancion... #" ;
      byte[] sendData = mensaje.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
      clientSocket.send(sendPacket);

      byte[] buffer = new byte[BUFFER_SIZE];
  

      int count = 0;
      while (true) {

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        clientSocket.receive(packet);
        buffer = packet.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        int packetCount = byteBuffer.getInt();
        if (packetCount == -1) {

          //System.out.println("Received last packet " + count);
          break;

        } else {

          byte[] data = new byte[1024];
          byteBuffer.get(data, 0, data.length);
          playerThread.addBytes(data);
          
          //System.out.println("Received packet " + packetCount + " current: " + count);

        }
        count++;

      }

      clientSocket.close();

    }


}