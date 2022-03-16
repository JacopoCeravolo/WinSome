package client;

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
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import shared.communication.Protocol;
import shared.rmi.FollowersUpdateInterface;
import shared.rmi.RMIRegistrationInterface;


public class ClientMain {
    
    private final static String DELIMITER = " ";
    private final static String PROMPT = ">> ";

    static final Pattern ARG_PAT = Pattern.compile("\"[^\"]+\"|\\S+");

    private final static int SERVER_PORT = 6789;
    private final static int RMI_PORT = 6889;
    private final static int CALLBACK_PORT = 6989;

    private final static String CALLBACK_SERVICE_NAME = "WinSomeCallback";
    public final static String RMI_SERVICE_NAME = "WinSomeRegistration";
    public static ArrayList<String> followersList;

    public static String[] parseArgs(String message) {
        return ARG_PAT.matcher(message)
            .results()
            .map(r -> r.group())
            .toArray(String[]::new);
    }

    public static void main(String[] args) {

        welcomeMsg();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String keyboardInput = null;
        
        // String activeUsername = new String();
        Socket serverSocket = null;
        BufferedReader serverInput = null;
        PrintWriter serverOutput = null;
        FollowersUpdate callbackUpdate = null;
        Registry RMI_REGISTRY = null;
        RMIRegistrationInterface REGISTRATION_STUB = null;
        // CallbackRegistrationInterface CALLBACK_SERVICE = null;
        FollowersUpdateInterface FOLLOWERS_UPDATE = null;
        boolean exit = false;

        String activeUser = null;

        while (!exit) {

            System.out.printf(PROMPT);

            try {
                keyboardInput = stdIn.readLine();
            } catch (IOException e) {
                System.err.println("could not read from keyboard");
                System.exit(1);
            }

            StringTokenizer keyboardParser = new StringTokenizer(keyboardInput, DELIMITER);

            String action = keyboardParser.nextToken();
            
            switch (action) {
                
                case "help": {
                    helpMsg();
                    break;
                }

                /* case "exit": {
                    exit = true;

                    serverOutput.println("logout");
                    // TODO: should do half close
                    String serverResponse = null;
                    try {
                        serverResponse = Communication.receiveResponse(serverInput);
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
                } */

                case "register": {

                    String username = keyboardParser.nextToken();
                    String password = keyboardParser.nextToken();

                    ArrayList<String> tagsList = new ArrayList<>();

                    while (keyboardParser.hasMoreTokens()) {
                        tagsList.add(keyboardParser.nextToken());
                    }

                    // RMI
                    try {

                        RMI_REGISTRY = LocateRegistry.getRegistry(RMI_PORT);

                        REGISTRATION_STUB = (RMIRegistrationInterface) RMI_REGISTRY.lookup(RMI_SERVICE_NAME);
                        
                        REGISTRATION_STUB.registerUser(username, password, tagsList);

                    } catch (RemoteException e) {
                        System.err.println("Error: " + e.getLocalizedMessage());
                    } catch (NotBoundException e) {
                        System.err.println(RMI_SERVICE_NAME + " unknown service name");
                    }

                    break;
                }

                case "login": {

                    String username = keyboardParser.nextToken();
                    String password = keyboardParser.nextToken();

                    if (keyboardParser.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          login <username> <password>");
                        break;
                    }

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

                    Protocol.sendRequest(serverOutput, action, username, password);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
                    
                    activeUser = username;

                    try {

                        followersList = new ArrayList<>();
                        // Exporting object 
                        callbackUpdate = new FollowersUpdate();
                        FOLLOWERS_UPDATE = (FollowersUpdateInterface) 
                            UnicastRemoteObject.exportObject(callbackUpdate, 0);

                        REGISTRATION_STUB.registerForCallaback(username, FOLLOWERS_UPDATE);

                        List<String> newFollowers = REGISTRATION_STUB.pullUpdates(username);

                        for (String follower : newFollowers) {
                            followersList.add(follower);
                        }
                        
                    } catch (RemoteException e) {
                        System.err.println("Error: " + e.getLocalizedMessage());
                    } 

                    Thread walletUpdate = new Thread(new MulticastReceiver());
                    walletUpdate.start();

                    
                    System.out.println(serverResponse);
                    break;
                }

                case "logout": {

                    if (serverSocket == null) {
                        System.out.println("Error, not conncted to WinSome");
                        break;
                    }

                    if (activeUser == null) {
                        System.out.println("Error, no one logged in");
                        break;
                    }

                    try {
                        REGISTRATION_STUB.unregisterForCallback(activeUser, FOLLOWERS_UPDATE);
                    } catch (RemoteException e) {
                        System.err.println("could not unsubscribe from callback");
                    }

                    followersList = null;
                    callbackUpdate = null;
                    FOLLOWERS_UPDATE = null;

                    Protocol.sendRequest(serverOutput, action);

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

                    String userToFollow = keyboardParser.nextToken();

                    if (keyboardParser.hasMoreTokens()) {
                        
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          follow <username>");
                        break;
                    }
                    
                    Protocol.sendRequest(serverOutput, action, userToFollow);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "unfollow": {

                    String userToUnfollow = keyboardParser.nextToken();

                    if (keyboardParser.hasMoreTokens()) {
                        
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          unfollow <username>");
                        break;
                    }
                    
                    Protocol.sendRequest(serverOutput, action, userToUnfollow);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "post": {

                    String[] arguments = parseArgs(keyboardInput);

                    String title = arguments[1].substring(1, arguments[1].length() - 1);
                    String contents = arguments[2].substring(1, arguments[2].length() - 1);

                    /* if (keyboardParser.hasMoreTokens()) {
                        
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          post <title> <contents>");
                        break;
                    } */

                    Protocol.sendRequest(serverOutput, action, title, contents);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "delete": {

                    Integer idPost = null;

                    try {
                        idPost = Integer.parseInt(keyboardParser.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          delete <idPost>");
                        break;
                    }

                    if (keyboardParser.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          delete <idPost>");
                        break;
                    }
                    
                    Protocol.sendRequest(serverOutput, action, String.valueOf(idPost));

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "rate": {

                    Integer idPost = null;
                    Integer vote = null;

                    try {
                        idPost = Integer.parseInt(keyboardParser.nextToken());
                        vote = Integer.parseInt(keyboardParser.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          rate <idPost> <vote>  (+1 upvote, -1 downvote)");
                        break;
                    }

                    if (keyboardParser.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          rate <idPost> <vote>");
                        break;
                    }
                    
                    Protocol.sendRequest(serverOutput, action, String.valueOf(idPost), String.valueOf(vote));

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "rewin": {

                    Integer idPost = null;

                    try {
                        idPost = Integer.parseInt(keyboardParser.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          rewin <idPost>");
                        break;
                    }

                    if (keyboardParser.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          rewin <idPost>");
                        break;
                    }
                    
                    Protocol.sendRequest(serverOutput, action, String.valueOf(idPost));

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "comment": {

                    Integer idPost = null;

                    try {
                        idPost = Integer.parseInt(keyboardParser.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Expecting numeric value:");
                        System.out.println("          comment <idPost> <comment>");
                        break;
                    }

                    
                    String comment = keyboardParser.nextToken();


                    if (keyboardParser.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          comment <idPost> <comment>");
                        break;
                    }
                    
                    Protocol.sendRequest(serverOutput, action, String.valueOf(idPost), comment);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "blog": {

                    if (keyboardParser.hasMoreTokens()) {
                        System.out.println("Please use the correct syntax:");
                        System.out.println("          blog");
                        break;
                    }
                    
                    Protocol.sendRequest(serverOutput, action);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

                    System.out.println(serverResponse);
                    break;
                }

                case "show": {

                    String subcase = keyboardParser.nextToken();

                    switch (subcase) {
                        
                        case "feed": {

                            if (keyboardParser.hasMoreTokens()) {
                                System.out.println("Please use the correct syntax:");
                                System.out.println("          show feed");
                                break;
                            }

                            Protocol.sendRequest(serverOutput, action + subcase);

                            String serverResponse = null;
                            try {
                                serverResponse = Protocol.receiveResponse(serverInput);
                            } catch (IOException e) {
                                System.err.println("could not read from socket");
                            }
        
                            // TODO: check request type (errors etc)
                    
        
                            System.out.println(serverResponse);
                            break;
                        }
                    
                        case "post": {

                            Integer idPost = null;

                            try {
                                idPost = Integer.parseInt(keyboardParser.nextToken());
                            } catch (NumberFormatException e) {
                                System.out.println("Expecting numeric value:");
                                System.out.println("          show post <idPost>");
                                break;
                            }
                        
                            if (keyboardParser.hasMoreTokens()) {
                                System.out.println("Please use the correct syntax:");
                                System.out.println("          show post <idPost>");
                                break;
                            }

                            Protocol.sendRequest(serverOutput, action, String.valueOf(idPost));
                        
                            String serverResponse = null;
                            try {
                                serverResponse = Protocol.receiveResponse(serverInput);
                            } catch (IOException e) {
                                System.err.println("could not read from socket");
                            }
                        
                            // TODO: check request type (errors etc)
                        
                        
                            System.out.println(serverResponse);
                            break;
                        }
                        
                        default: {
                            System.err.println("Error, uknown action");
                            break;
                        }
                            
                    }

                    break;
                }

                case "list": {

                    String subcase = keyboardParser.nextToken();

                    switch (subcase) {

                        case "users": {

                            if (keyboardParser.hasMoreTokens()) {
                                System.out.println("Please use the correct syntax:");
                                System.out.println("          list users");
                                break;
                            }

                            Protocol.sendRequest(serverOutput, action + subcase);

                            String serverResponse = null;
                            try {
                                serverResponse = Protocol.receiveResponse(serverInput);
                            } catch (IOException e) {
                                System.err.println("could not read from socket");
                            }
        
                            // TODO: check request type (errors etc)
                    
        
                            System.out.println(serverResponse);
                            break;
                        }

                        case "following": {

                            if (keyboardParser.hasMoreTokens()) {
                                System.out.println("Please use the correct syntax:");
                                System.out.println("          list following");
                                break;
                            }

                            Protocol.sendRequest(serverOutput, action + subcase);

                            String serverResponse = null;
                            try {
                                serverResponse = Protocol.receiveResponse(serverInput);
                            } catch (IOException e) {
                                System.err.println("could not read from socket");
                            }
        
                            // TODO: check request type (errors etc)
                    
        
                            System.out.println(serverResponse);
                            break;
                        }

                        case "followers": {

                            System.out.println("<< showing your followers\n");

                            for (String user : followersList) {
                                System.out.println(user);
                            }

                            break;
                        }
                            
                        default:  {
                            System.err.println("Error, unkown action");
                            break;
                        }  
                    }
                    break;
                }

                case "wallet": {

                    Protocol.sendRequest(serverOutput, action);

                    String serverResponse = null;
                    try {
                        serverResponse = Protocol.receiveResponse(serverInput);
                    } catch (IOException e) {
                        System.err.println("could not read from socket");
                    }

                    // TODO: check request type (errors etc)
            

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
