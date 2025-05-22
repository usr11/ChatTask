import java.io.*;
import java.net.*;

public class Lector implements Runnable{
    String message;
    BufferedReader in;
    public Lector(BufferedReader in){
        this.in=in;
    }

    @Override
    public void run() {
        //leer la linea que envia el servidor e imprimir en pantalla
        try { 
            
            while ((message = in.readLine()) != null) {
                System.out.println(message);
                System.out.println("[1] Para mensaje - [2] Para audio - [3] Para salir");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }      
    
    }

}