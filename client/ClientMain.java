package client;

import shared.rmi.*;
import shared.communication.*;
import shared.utils.*;

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
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

public class ClientMain {
    
    private final static String DELIMITER = " ";
    private final static String PROMPT = ">> ";
    private final static int SERVER_PORT = 6789;
    private final static int RMI_PORT = 6889;

    public static void main(String[] args) {

        System.out.println("");
        System.out.println(" ------------------");
        System.out.println("|    WELCOME TO    |");
        System.out.println("|  WINSOME CLIENT  |");
        System.out.println(" ------------------\n");

        System.out.println("Type 'help' to see list of commands\n");

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String keyboardInput = null;
        
        Socket serverSocket = null;
        BufferedReader serverInput = null;
        PrintWriter serverOutput = null;
        boolean exit = false;

        while (!exit) {

            System.out.printf(PROMPT);

            try {
                keyboardInput = stdIn.readLine();
            } catch (IOException e) {
                System.err.println("could not read from keyboard");
                System.exit(1);
            }

            StringTokenizer tokenizedInput = new StringTokenizer(keyboardInput, DELIMITER);

            
            switch (tokenizedInput.nextToken()) {
                
                case "help": {

                    System.out.println(">> list of commands");

                    break;
                }

                case "exit": {
                    exit = true;
                    break;
                }

                case "register": {

                    String username = tokenizedInput.nextToken();
                    String password = tokenizedInput.nextToken();

                    ArrayList<String> tagsList = new ArrayList<>();

                    while (tokenizedInput.hasMoreTokens()) {
                        tagsList.add(tokenizedInput.nextToken());
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

                    break;
                }

                case "login": {

                    try {
                        serverSocket = new Socket("localhost", SERVER_PORT);  
                        serverInput = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                        serverOutput = new PrintWriter(serverSocket.getOutputStream(), true);
                    } catch (UnknownHostException e) {
                        System.err.println("Unknown host");
                        exit = true;
                        break;
                    } catch (IOException e) {
                        exit = true;
                        e.printStackTrace();
                        break;
                    }

                    serverOutput.println(keyboardInput);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    System.out.println(serverResponse);

                    break;
                }

                case "logout": {

                    serverOutput.println(keyboardInput);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    System.out.println(serverResponse);

                    try {
                        serverSocket.close();
                        serverInput.close();
                        serverOutput.close();
                    } catch (IOException e) {
                        System.err.println("error when closing files");
                    }
                    

                    break;
                }
            
                default: {

                    serverOutput.println(keyboardInput);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    System.out.println(serverResponse);

                    break;
                }
            }
        }
    
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("error when closing socket");
            }
        }

        if (serverInput != null) {
            try {
                serverInput.close();
            } catch (IOException e) {
                System.err.println("error when closing socket");
            }
        }

        if (serverOutput != null) {
            serverOutput.close();
        }

        System.out.println(PROMPT + "Bye bye!\n");

    }
}
