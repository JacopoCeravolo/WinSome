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


public class ClientMain {
    
    private final static String DELIMITER = " ";
    private final static String PROMPT = ">> ";
    private final static int SERVER_PORT = 6789;
    private final static int RMI_PORT = 6889;

    public static void main(String[] args) {

        welcomeMsg();

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
                    helpMsg();
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

                    // TODO: move socket reading/writing to communication package
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
            
                case "follow": {

                    String userToFollow = tokenizedInput.nextToken();

                    if (tokenizedInput.hasMoreTokens()) {
                        
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          follow <username>");
                        break;
                    }
                    
                    // TODO: move socket reading/writing to communication package
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

                case "unfollow": {

                    String userToUnfollow = tokenizedInput.nextToken();

                    if (tokenizedInput.hasMoreTokens()) {
                        
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          unfollow <username>");
                        break;
                    }
                    
                    // TODO: move socket reading/writing to communication package
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

                case "post": {
                    // TODO: move socket reading/writing to communication package
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

                case "delete": {

                    Integer idPost = null;

                    try {
                        idPost = Integer.parseInt(tokenizedInput.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          delete <idPost>");
                        break;
                    }

                    if (tokenizedInput.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          delete <idPost>");
                        break;
                    }
                    
                    // TODO: move socket reading/writing to communication package
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

                case "rate": {

                    Integer idPost = null;
                    Integer vote = null;

                    try {
                        idPost = Integer.parseInt(tokenizedInput.nextToken());
                        vote = Integer.parseInt(tokenizedInput.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          rate <idPost> <vote>  (+1 upvote, -1 downvote)");
                        break;
                    }

                    if (tokenizedInput.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          rate <idPost> <vote>");
                        break;
                    }
                    
                    // TODO: move socket reading/writing to communication package
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

                case "rewin": {

                    Integer idPost = null;

                    try {
                        idPost = Integer.parseInt(tokenizedInput.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          rewin <idPost>");
                        break;
                    }

                    if (tokenizedInput.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          rewin <idPost>");
                        break;
                    }
                    
                    // TODO: move socket reading/writing to communication package
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

                case "comment": {

                    Integer idPost = null;

                    try {
                        idPost = Integer.parseInt(tokenizedInput.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          comment <idPost> <comment>");
                        break;
                    }

                    
                    String comment = tokenizedInput.nextToken();


                    if (tokenizedInput.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          comment <idPost> <comment>");
                        break;
                    }
                    
                    // TODO: move socket reading/writing to communication package
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

                case "blog": {
                    // TODO: move socket reading/writing to communication package
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

                case "show": {
                    // TODO: move socket reading/writing to communication package
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

                case "list": {
                    // TODO: move socket reading/writing to communication package
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

                case "wallet": {
                    // TODO: move socket reading/writing to communication package
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

                default: {

                    System.out.println("["+keyboardInput+"]" + " - unknown sequence");
                    System.out.printf("\nType 'help' to see list of commands\n\n");

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

        System.out.println(PROMPT + "Bye, bye!\n");

    }

    public static void welcomeMsg() {

        System.out.printf("\n");
        System.out.printf("                 ----------------------------------------\n");
        System.out.printf("                |               WELCOME TO               |\n");
        System.out.printf("                |              WINSOME CLIENT            |\n");
        System.out.printf("                 ----------------------------------------\n");

        System.out.printf("\nType 'help' to see list of commands\n\n");

    }

    public static void helpMsg() {
        System.out.printf(" -----------------------------------------------------------------------------\n");
        System.out.printf("|                               REGISTRATION                                  |\n");
        System.out.printf(" -----------------------------------------------------------------------------\n");
        System.out.printf("| If you haven't registered yet, create an account to start using WinSome     |\n");
        System.out.printf("| Type the following command:                                                 |\n");
        System.out.printf("|          register <username> <password> <tag1> ... <tag5>                   |\n");
        System.out.printf("| With this command you create a new user with specified <username> and       |\n");
        System.out.printf("| <password>. You also have to list up to 5 tags which allows you to find     |\n");
        System.out.printf("| users with your same interests. Tags are made of a single word such as      |\n");
        System.out.printf("| music, sports, politics...                                                  |\n");
        System.out.printf(" -----------------------------------------------------------------------------\n");

        System.out.printf(" -----------------------------------------------------------------------------\n");
        System.out.printf("|     COMMAND    |      ARGUMENTS    |               DESCRIPTION              |\n");
        System.out.printf(" -----------------------------------------------------------------------------\n");
        System.out.printf("| login          | username password | login user with given credentials      |\n");        
        System.out.printf("| logout         |                   | logout current user                    |\n");        
        System.out.printf("| list users     |                   | show users with same tags              |\n");        
        System.out.printf("| list following |                   | show users you're following            |\n");        
        System.out.printf("| list followers |                   | show users who follow you              |\n");        
        System.out.printf("| follow         | username          | follow user                            |\n");        
        System.out.printf("| unfollow       | username          | unfollow user                          |\n");        
        System.out.printf("| blog           |                   | view your blog                         |\n");        
        System.out.printf("| post           | title contents    | create a new post on your blog         |\n");        
        System.out.printf("| show feed      |                   | view posts from users you follow       |\n");        
        System.out.printf("| show post      | idPost            | show a specific post                   |\n");        
        System.out.printf("| delete         | idPost            | delete a post from your blog           |\n");
        System.out.printf("| rewin          | idPost            | rewin a post                           |\n");        
        System.out.printf("| rate           | idPost vote       | rate a post: (+1) upvote (-1) downvote |\n");        
        System.out.printf("| comment        | idPost comment    | write a comment to a post              |\n");        
        System.out.printf("| wallet         |                   | show your balance in wincoins          |\n");       
        System.out.printf("| wallet btc     |                   | show your balance in bitcoins          |\n");
        System.out.printf(" -----------------------------------------------------------------------------\n");
        
    }
}
