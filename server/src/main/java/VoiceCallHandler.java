import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class VoiceCallHandler implements Runnable {
    private Socket clientSocket;
    private InputStream input;
    private OutputStream out;
    private TargetDataLine targetDataLine;
    private SourceDataLine sourceDataLine;
    private AudioFormat audioFormat;
    private static final int BUFFER_SIZE = 10000;

    public VoiceCallHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            setupAudio();
            input = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());

            // Hilo para capturar y enviar audio
            new Thread(this::captureAudio).start();

            // Reproducir audio recibido
            byte[] tempBuffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = input.read(tempBuffer)) != -1) {
                sourceDataLine.write(tempBuffer, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAudio() throws LineUnavailableException {
        audioFormat = new AudioFormat(8000.0f, 8, 1, true, false);

        DataLine.Info outInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(outInfo);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        DataLine.Info inInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(inInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
    }

    private void captureAudio() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (true) {
                int count = targetDataLine.read(buffer, 0, buffer.length);
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
