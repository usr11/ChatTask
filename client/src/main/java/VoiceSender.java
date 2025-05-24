import javax.sound.sampled.*;
import java.io.OutputStream;
import java.net.Socket;

public class VoiceSender implements Runnable {

    private final Socket socket;

    public VoiceSender(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try (OutputStream out = socket.getOutputStream()) {
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[10000];
            int bytesRead;

            System.out.println("Capturando y enviando audio...");

            while (true) {
                bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("Error en VoiceSender: " + e.getMessage());
        }
    }
}
