import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHandler implements Runnable{

    private final static String DELIMITER = " ";

    private Socket clientConnection;
    // private ConcurrentHashMap<String, WinSomeUser> usersMap = new ConcurrentHashMap<>();
    private WinSomeNetwork network = new WinSomeNetwork();

    public RequestHandler(Socket clientConnection, WinSomeNetwork network) {
        this.clientConnection = clientConnection;
        this.network = network;
    }

    @Override
    public void run() {

        System.out.println("New connection on port " + clientConnection.getPort());
        
        try (BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
             PrintWriter clientOutput = new PrintWriter(clientConnection.getOutputStream(), true)) {

            // Connection variables
            WinSomeUser activeUser = null;
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
                    
                    /* case "register": {
                        
                        String reg_username = (String)requestLine.nextToken();
                        String reg_password = (String)requestLine.nextToken();

                        LinkedList<String> tagsList = new LinkedList<>();

                        while (requestLine.hasMoreElements()) { // TODO: max 5 tags
                            tagsList.add((String)requestLine.nextToken());
                        }

                        WinSomeUser newUser = new WinSomeUser(reg_username, reg_password, tagsList);

                        network.register(newUser);
                       
                        responseLine.append("registration successfull");

                        System.out.println(responseLine);

                        break;
                    }
                     */
                    
                     case "login": {

                        if (username != null || password != null) {
                            responseLine.append(username + " is currently logged in");
                            break;
                        }

                        username = (String)requestLine.nextToken();
                        password = (String)requestLine.nextToken();

                        try {
                            activeUser = network.login(username, password);
                        } catch (UserNotFoundException e) {
                            responseLine.append("user not found");
                            break;
                        } catch (InvalidPasswordException e) {
                            responseLine.append("invalid password");
                            break;
                        }

                        responseLine.append("user "+username+" logged in");

                        break;
                    }

                    case "logout": {
                        
                        if (username == null || password == null) {
                            responseLine.append("no user is logged in");
                            break;
                        }

                        network.logout(activeUser);

                        responseLine.append("user "+username+" logged out");

                        endConnection = true;

                        break;
                    }
                        
                    case "list": {
                        
                        String subcase = requestLine.nextToken();

                        switch (subcase) {
                            case "users": {
                                
                                StringBuilder usersList = network.listUsers(activeUser);

                                System.out.println(usersList);

                                responseLine.append("IN PROGRESS (list users)");
                                break;
                            }
                            case "followers": {

                                // TODO: temporary, move to callback
                                HashMap<String, WinSomeUser> followers = activeUser.getFollowers();
                                
                                System.out.println(followers);

                                responseLine.append("IN PROGRESS (list followers)");
                                break;
                            }
                            case "following": {
                                
                                HashMap<String, WinSomeUser> following = activeUser.getFollowing();
                                
                                System.out.println(following);

                                responseLine.append("IN PROGRESS (list following)");
                                break;
                            }     

                            default:
                                break;
                        }

                        break;
                    }
  
                    case "follow": {

                        String toFollow = (String)requestLine.nextToken();

                        if (toFollow.equals(activeUser.getUserName())) {
                            responseLine.append("cannot follow yourself");
                            break;
                        }

                        try {
                            network.followUser(activeUser, toFollow);
                        } catch (UserNotFoundException e) {
                            responseLine.append("user not found");
                            break;
                        }

                        responseLine.append("following user "+toFollow);

                        break;
                    }
                    
                    case "unfollow": {

                        String toUnfollow = (String)requestLine.nextToken();

                        try {
                            network.unfollowUser(activeUser, toUnfollow);
                        } catch (UserNotFoundException e) {
                            responseLine.append("user not found");
                            break;
                        }

                        responseLine.append("user "+toUnfollow+ " unfollowed");

                        break;
                    }
                    
                    case "post": {
                        
                        String title = (String)requestLine.nextToken();
                        
                        String contents = (String)requestLine.nextToken();

                        WinSomePost newPost = new WinSomePost(activeUser, title, contents);

                        Integer postID = network.createPost(activeUser, newPost);

                        responseLine.append("post created (id="+postID+")");

                        break;
                    }

                    case "blog": {

                        HashMap<Integer, WinSomePost> blog = activeUser.getBlog();

                        System.out.println(blog);

                        responseLine.append("IN PROGRESS (blog)");

                        break;
                    }

                    case "show": {
                        
                        String subcase = (String)requestLine.nextToken();

                        switch (subcase) {
                            case "feed": {
                                ArrayList<WinSomePost> feed = network.getFeed(activeUser);
        
                                System.out.println(feed);

                                responseLine.append("IN PROGRESS (show feed)");
                                
                                break;
                            }                     
                            case "post": {
                                
                                Integer postID = Integer.parseInt(requestLine.nextToken());
                
                                WinSomePost post = null;

                                try {
                                    post = network.getPost(postID);
                                } catch (PostNotFoundException e) {
                                    responseLine.append("post "+postID+" not found");
                                    break;
                                }
                                
                                System.out.println(post);

                                responseLine.append("IN PROGRESS (show post)");

                                break;
                            }                        
                            
                            default:
                                break;
                        }

                        break;
                    }
                    
                    case "delete": {

                        Integer postID = Integer.parseInt(requestLine.nextToken());

                        WinSomePost post = null;

                        try {
                            post = network.deletePost(activeUser, postID);
                        } catch (PostNotFoundException e) {
                            responseLine.append("post "+postID+" not found");
                            break;
                        } catch (UnauthorizedOperationException e) {
                            responseLine.append("unauthorized to delete post "+postID);
                            break;
                        }

                        responseLine.append("post (id="+postID+") deleted");

                        break;
                    }

                    case "rewin": {
                        
                        Integer postID = Integer.parseInt(requestLine.nextToken());

                        try {
                            network.rewinPost(activeUser, postID);
                        } catch (PostNotFoundException e) {
                            responseLine.append("post (id="+postID+") not found");
                            break;
                        }

                        responseLine.append("post (id="+postID+") added to blog");

                        break;
                    }
                        
                    case "rate": {

                        Integer postID = Integer.parseInt(requestLine.nextToken());
                        Integer vote = Integer.parseInt(requestLine.nextToken());

                        try {
                            network.ratePost(activeUser, postID, vote);
                        } catch (PostNotFoundException e) {
                            responseLine.append("post (id="+postID+") not found");
                            break;
                        } catch (UnauthorizedOperationException e) {
                            responseLine.append("unauthorized to vote post (id="+postID+")");
                            break;
                        }
                        
                        responseLine.append("voted post (id="+postID+")");

                        break;
                    }

                    case "comment": {

                        Integer postID = Integer.parseInt(requestLine.nextToken());
                        String comment = requestLine.nextToken();

                        try {
                            network.commentPost(activeUser, postID, comment);
                        } catch (PostNotFoundException e) {
                            responseLine.append("post (id="+postID+") not found");
                            break;
                        } catch (UnauthorizedOperationException e) {
                            responseLine.append("unauthorized to comment post (id="+postID+")");
                            break;
                        }

                        responseLine.append("added comment to post (id="+postID+")");

                        break;
                    }
                    
                    case "wallet": {

                        if (requestLine.hasMoreTokens()) {
                            responseLine.append("IN PROGRESS (wallet btc)");
                        } else {
                            responseLine.append("IN PROGRESS (wallet)");
                        }

                        break;
                    }

                    default:
                        break;
                }

                System.out.println(responseLine);
                clientOutput.println(responseLine);
                responseLine.setLength(0); // empty stringbuilder
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
