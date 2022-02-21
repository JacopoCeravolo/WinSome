import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHandler implements Runnable{

    private final static String DELIMITER = " ";

    private Socket clientConnection;
    private ConcurrentHashMap<String, WinSomeUser> usersMap = new ConcurrentHashMap<>();

    public RequestHandler(Socket clientConnection, ConcurrentHashMap<String, WinSomeUser> usersMap) {
        this.clientConnection = clientConnection;
        this.usersMap = usersMap;
    }

    @Override
    public void run() {

        System.out.println("New connection on port " + clientConnection.getPort());
        
        try (BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
             PrintWriter clientOutput = new PrintWriter(clientConnection.getOutputStream(), true)) {

            // Connection variables
            Boolean endConnection = false;
            String username = null;
            String password = null;

            while (!endConnection) {
                String requestString = clientInput.readLine();
                StringTokenizer requestLine = new StringTokenizer(requestString, DELIMITER);
                StringBuilder responseLine = new StringBuilder("<< ");

                System.out.println(">> " + requestString);

                String requestType = requestLine.nextToken();

                switch (requestType) {
                    case "register": {
                        
                        String reg_username = (String)requestLine.nextToken();
                        String reg_password = (String)requestLine.nextToken();

                        LinkedList<String> tagsList = new LinkedList<>();

                        while (requestLine.hasMoreElements()) { // TODO: max 5 tags
                            tagsList.add((String)requestLine.nextElement());
                        }

                        WinSomeUser newUser = new WinSomeUser(reg_username, reg_password, tagsList);
                        usersMap.putIfAbsent(newUser.getUserName(), newUser);

                        responseLine.append("registration successfull");

                        break;
                    }
                    case "login": {

                        if (username != null || password != null) {
                            responseLine.append(username + " is currently logged in");
                            break;
                        }

                        username = (String)requestLine.nextToken();
                        password = (String)requestLine.nextToken();

                        WinSomeUser user = usersMap.get(username);
                        
                        if (user == null) {
                            responseLine.append("user not found");
                            break;
                        }

                        if (!password.equals(user.getPassword())) {
                            responseLine.append("invalid password");
                            break;
                        }

                        if (user.getUserStatus().equals(WinSomeUserStatus.ONLINE)) {
                            responseLine.append("user already logged in");
                            break;
                        }

                        user.setUserStatus(WinSomeUserStatus.ONLINE);

                        responseLine.append("user " + username + " logged in");

                        break;
                    }

                    case "logout": {
                        
                        if (username == null || password == null) {
                            responseLine.append("no user is logged in");
                            break;
                        }

                        WinSomeUser user = usersMap.get(username);
                        
                        if (user == null) {
                            // throw exception
                        }

                        user.setUserStatus(WinSomeUserStatus.OFFLINE);

                        responseLine.append("user " + username + " logged out");

                        endConnection = true;

                        break;
                    }
                        
                    case "show":
                        
                        StringBuilder usersList = new StringBuilder();

                        // responseLine.append("winsome users\n");

                        for (WinSomeUser user : usersMap.values()) {
                            responseLine.append(user.toString());
                        }

                        
                        break;

                    case "post":

                        break;
                    case "comment":

                        break;
                    
                    default:
                        break;
                }

                clientOutput.println(responseLine);
                responseLine.setLength(0); // empty stringbuilder
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
