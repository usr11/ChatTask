import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import javax.sound.sampled.*;

public class Client {
    private static final String SERVER_IP = "172.20.10.3";
    private static final int PORT = 6789;
    private static final int AUDIO_PORT = 6790;

    static Socket socket;
    static PrintWriter out;
    static BufferedReader in;
    static BufferedReader userInput;
    static String message;

    static boolean stopCapture = false;
    static ByteArrayOutputStream byteArrayOutputStream;
    static AudioFormat audioFormat;
    static TargetDataLine targetDataLine;
    static AudioInputStream audioInputStream;
    static BufferedOutputStream audioOut = null;
    static BufferedInputStream audioIn = null;
    static Socket audioSocket = null;
    static SourceDataLine sourceDataLine;

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
            System.out.println("[1] Para mensaje - [2] Para audio en tiempo real - [3] Para salir");

            while (option != 3) {
                String optionInput = userInput.readLine();
                option = Integer.parseInt(optionInput);

                switch (option) {
                    case 1:
                        textMessage();
                        break;
                    case 2:
                        startRealtimeAudio();
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

    public static void startRealtimeAudio() {
        try {
            audioSocket = new Socket(SERVER_IP, AUDIO_PORT);
            audioOut = new BufferedOutputStream(audioSocket.getOutputStream());
            audioIn = new BufferedInputStream(audioSocket.getInputStream());

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                System.out.println(mixerInfo[cnt].getName());
            }

            audioFormat = new AudioFormat(8000.0F, 8, 1, true, false);

            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            Thread captureThread = new Thread(() -> {
                byte[] tempBuffer = new byte[10000];
                try {
                    while (true) {
                        int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                        audioOut.write(tempBuffer, 0, cnt);
                        audioOut.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Error capturando audio: " + e.getMessage());
                }
            });
            captureThread.start();

            DataLine.Info dataLineInfo1 = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo1);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            Thread playThread = new Thread(() -> {
                byte[] tempBuffer = new byte[10000];
                try {
                    while (audioIn.read(tempBuffer) != -1) {
                        sourceDataLine.write(tempBuffer, 0, 10000);
                    }
                    sourceDataLine.drain();
                    sourceDataLine.close();
                } catch (IOException e) {
                    System.out.println("Error reproduciendo audio: " + e.getMessage());
                }
            });
            playThread.start();

        } catch (Exception e) {
            System.out.println("Error en comunicación de audio: " + e.getMessage());
        }
    }
} class Lector implements Runnable {
    private BufferedReader in;

    public Lector(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        String mensaje;
        try {
            while ((mensaje = in.readLine()) != null) {
                System.out.println(mensaje);
            }
        } catch (IOException e) {
            System.out.println("Error en Lector: " + e.getMessage());
        }
    }
}