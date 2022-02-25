package client;

import shared.rmi.*;
import shared.communication.*;
import shared.utils.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ClientMain {
    
    private final static int SERVER_PORT = 6789;
    private final static int RMI_PORT = 6889;

    public static void main(String[] args) {
        
        try (Socket serverSocket = new Socket("localhost", SERVER_PORT);
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

                StringTokenizer tokenizedString = new StringTokenizer(request, " ");

                if (tokenizedString.nextToken().equals("register")) {

                    String username = tokenizedString.nextToken();
                    String password = tokenizedString.nextToken();

                    ArrayList<String> tagsList = new ArrayList<>();

                    while (tokenizedString.hasMoreTokens()) {
                        tagsList.add(tokenizedString.nextToken());
                    }

                    Registry RMI_REGISTRY = null;
                    RMIRegistrationInterface STUB = null;

                    try {

                        RMI_REGISTRY = LocateRegistry.getRegistry(RMI_PORT);
                        STUB = (RMIRegistrationInterface) RMI_REGISTRY.lookup(Utils.RMI_SERVICE_NAME);
                        STUB.registerUser(username, password, tagsList);

                    } catch (RemoteException e) {
                        System.err.println("Error: " + e.getLocalizedMessage());
                    } catch (NotBoundException e) {
                        System.err.println(Utils.RMI_SERVICE_NAME + " unknown service name");
                    }

                    continue;
                }

                serverOutput.println(request);

                String response = null;
                try {
                    response = Protocol.receiveResponse(serverInput);
                } catch (IOException e) {
                    System.err.println("could not read from socket");
                }

                System.out.println(response);
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
