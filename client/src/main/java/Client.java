import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "172.30.162.33";
    private static final int PORT = 6789;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            System.out.println("Conectado al servidor.");

            String message;
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
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

            //estar atento a la entrada del usuario          
            System.out.println("Ingrese el mensaje a enviar (@nombre para enviar por privado)");
            while ((message = userInput.readLine()) != null) {
                System.out.println("Ingrese el mensaje (@nombre para enviar por privado)");
                out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}