import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientMain {
    
    private final static int PORT = 6789;

    public static void main(String[] args) {
        
        try (Socket serverSocket = new Socket("localhost", 6789);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));   
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
             PrintWriter serverOutput = new PrintWriter(serverSocket.getOutputStream(), true);
             ){
            System.out.println("Connection enstablished");
            boolean end = false;

            
            while (!end) {

                System.out.printf(">> ");

                String request = stdIn.readLine();

                if (request.contentEquals("exit")) {
                    end = true; continue;
                }
                
                serverOutput.println(request);

                System.out.println(serverInput.readLine());
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
