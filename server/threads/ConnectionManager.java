package server.threads;


import server.rmi.RMIRegistration;
import server.socialnetwork.*;

import server.socialnetwork.exceptions.InvalidPasswordException;
import server.socialnetwork.exceptions.PostNotFoundException;
import server.socialnetwork.exceptions.UnauthorizedOperationException;
import server.socialnetwork.exceptions.UserNotFoundException;
import shared.communication.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionManager implements Runnable{

    private final static String DELIMITER = " ";
    private final static String PROMPT = "<< ";
    private final static String ERROR = "ERROR: ";

    private Socket clientConnection;
    private int port;
    private AtomicBoolean exitSignal;
    
    private WinSomeNetwork network;
    private RMIRegistration STUB;

    public ConnectionManager(Socket clientConnection, WinSomeNetwork network, 
        AtomicBoolean exitSignal, RMIRegistration STUB) {
        
        this.clientConnection = clientConnection;
        this.network = network;
        this.exitSignal = exitSignal;
        this.STUB = STUB;
        this.port = clientConnection.getPort();
    }

    @Override
    public void run() {

        String CONNECTION_NO = "[PORT "+port+"]";

        // System.out.println("New connection on port " + clientConnection.getPort());
        
        try (BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
             PrintWriter clientOutput = new PrintWriter(clientConnection.getOutputStream(), true)) {

            // Connection variables
            User activeUser = null;
            Boolean endConnection = false;
            String username = null;
            String password = null;

            while (!endConnection && !exitSignal.get()) {

                String request = Protocol.receiveRequest(clientInput);
                StringBuilder response = new StringBuilder(PROMPT);

                System.out.println(CONNECTION_NO + " received -> " + request);

                StringTokenizer requestParser = new StringTokenizer(request, Protocol.DELIMITER);

                String action = requestParser.nextToken();

                switch (action) {

                    case "login": {

                        if (username != null || password != null) {
                            response.append(username + " is currently logged in");
                            break;
                        }

                        username = requestParser.nextToken();
                        password = requestParser.nextToken();

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        try {
                            activeUser = network.login(username, password);
                        } catch (UserNotFoundException e) {
                            response.append(ERROR + "user not found");
                            break;
                        } catch (InvalidPasswordException e) {
                            response.append(ERROR + "invalid password");
                            break;
                        }

                        response.append("user " +username+ " logged in");
                        break;
                    }

                    case "logout": {

                        if (username == null || password == null) {
                            response.append(ERROR + "no user logged in");
                            break;
                        }

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        network.logout(activeUser);
                        endConnection = true;

                        response.append("user "+username+" logged out");
                        break;
                    }

                    case "listusers": {

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        response.append("showing users with same tags:\n");

                        ArrayList<User> usersList = network.listUsers(activeUser);

                        for (User user : usersList) {
                            response.append(user.toString());
                        }

                        break;
                    }

                    case "listfollowing": {

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        response.append("users you are following:\n");

                        ArrayList<String> followingList = activeUser.getFollowing();

                        for (String u : followingList) {

                            response.append(network.getUsersMap().get(u).toString());
                        }

                        break;
                    }

                    case "follow": {

                        String toFollow = requestParser.nextToken();

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }


                        if (toFollow.equals(activeUser.getUserName())) {
                            response.append(ERROR + "cannot follow yourself");
                            break;
                        }


                        try {
                            network.followUser(activeUser, toFollow);
                        } catch (UserNotFoundException e) {
                            response.append(ERROR + "user not found");
                            break;
                        }

                        try {
                            STUB.followerUpdate(toFollow, "add"+":"+activeUser.toString());
                        } catch (RemoteException e) {
                            System.err.println(e.getMessage());
                            System.err.println(e.getStackTrace());
                            System.err.println(e.getCause());
                            response.append(ERROR + "unable to send notification");
                            break;
                        } catch (NullPointerException e) {
                            System.err.println(e.getMessage());
                            response.append(ERROR + "cannote find user");
                            break;
                        }
                        
                        response.append("following user "+toFollow);
                    
                        break;
                    }

                    case "unfollow": {

                        String toUnfollow = requestParser.nextToken();

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        if (toUnfollow.equals(activeUser.getUserName())) {
                            response.append(ERROR + "cannot unfollow yourself");
                            break;
                        }

                        try {
                            network.unfollowUser(activeUser, toUnfollow);
                        } catch (UserNotFoundException e) {
                            response.append(ERROR + "user not found");
                            break;
                        }

                        try {
                            STUB.followerUpdate(toUnfollow, "remove"+":"+activeUser.toString());
                        } catch (RemoteException e) {
                            System.err.println(e.getMessage());
                            System.err.println(e.getStackTrace());
                            System.err.println(e.getCause());
                            response.append(ERROR + "unable to send notification");
                            break;
                        }
                        

                        response.append("user "+toUnfollow+ " unfollowed");
                        break;
                    }

                    case "post": {

                        String title = requestParser.nextToken();
                        String contents = requestParser.nextToken();

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        Post newPost = new Post(activeUser, title, contents);

                        Integer postID = network.createPost(activeUser, newPost);
                        
                        response.append("post (id="+postID+") created");
                        break;
                    }

                    case "blog": {

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        response.append("showing your blog:\n");

                        /* HashMap<Integer, Post> blog = activeUser.getBlog();

                        for (Post post : blog.values()) {
                            response.append(post.toString());
                        } */

                        break;
                    }

                    case "showfeed": {

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        response.append("showing your feed:\n");

                        ArrayList<Post> feed = network.showFeed(activeUser);
        
                        for (Post post : feed) {
                            response.append(post.toString());
                        }

                        break;
                    }

                    case "showpost": {

                        Integer postID = Integer.parseInt(requestParser.nextToken());

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }
                
                        Post post = null;
                        try {
                            post = network.showPost(postID);
                        } catch (PostNotFoundException e) {
                            response.append(ERROR + "post (id="+postID+") not found");
                            break;
                        }

                        response.append("showing post (id="+postID+")\n");
                        response.append(post.toString());
                        break;
                    }

                    case "delete": {

                        Integer postID = Integer.parseInt(requestParser.nextToken());

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        Post post = null;

                        try {
                            post = network.deletePost(activeUser, postID);
                        } catch (PostNotFoundException e) {
                            response.append(ERROR + "post (id="+postID+") not found");
                            break;
                        } catch (UnauthorizedOperationException e) {
                            response.append(ERROR + "unauthorized to delete post (id="+postID+")");
                            break;
                        }

                        response.append("post (id="+postID+") deleted");
                        break;
                    }

                    case "rewin": {
                        
                        Integer postID = Integer.parseInt(requestParser.nextToken());

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        try {
                            network.rewinPost(activeUser, postID);
                        } catch (PostNotFoundException e) {
                            response.append(ERROR + "post (id="+postID+") not found");
                            break;
                        }

                        response.append("post (id="+postID+") added to blog");
                        break;
                    }

                    case "rate": {

                        Integer postID = Integer.parseInt(requestParser.nextToken());
                        Integer vote = Integer.parseInt(requestParser.nextToken());

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        try {
                            network.ratePost(activeUser, postID, vote);
                        } catch (PostNotFoundException e) {
                            response.append(ERROR + "post (id="+postID+") not found");
                            break;
                        } catch (UnauthorizedOperationException e) {
                            response.append(ERROR + "unauthorized to vote post (id="+postID+")");
                            break;
                        }
                        
                        response.append("voted post (id="+postID+")");

                        break;
                    }

                    case "comment": {

                        Integer postID = Integer.parseInt(requestParser.nextToken());
                        String comment = requestParser.nextToken();

                        if (requestParser.hasMoreTokens()) {
                            response.append(ERROR + "too many arguments");
                            break;
                        }

                        try {
                            network.commentPost(activeUser, postID, comment);
                        } catch (PostNotFoundException e) {
                            response.append(ERROR + "post (id="+postID+") not found");
                            break;
                        } catch (UnauthorizedOperationException e) {
                            response.append(ERROR + "unauthorized to comment post (id="+postID+")");
                            break;
                        }

                        response.append("added comment to post (id="+postID+")");
                        break;
                    }

                    case "wallet": {

                        if (requestParser.hasMoreTokens()) {

                            response.append("IN PROGRESS (wallet btc)");

                        } else {
                    
                            Wallet wallet = activeUser.getWallet();
                            response.append("your current wallet history:\n");
                            response.append(wallet.viewHistory());
                        }

                        break;
                    }

                    default: {
                        System.err.println("Unrecognized client request");
                        request += ERROR + " unrecognized request";
                        break;
                    }
                }

                Protocol.sendResponse(clientOutput, response.toString());
                response.setLength(0);
                /* String requestString = clientInput.readLine();
                StringTokenizer requestLine = new StringTokenizer(requestString, DELIMITER);
                StringBuilder responseLine = new StringBuilder();

                System.out.println(">> " + requestString);

                String requestType = requestLine.nextToken();

                switch (requestType) {
                    
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
                                
                                ArrayList<WinSomeUser> usersList = network.listUsers(activeUser);

                                for (WinSomeUser user : usersList) {
                                    responseLine.append(user.toString());
                                }

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
                                
                                for (WinSomeUser user : following.values()) {
                                    responseLine.append(user.toString());
                                }

                                // responseLine.append("IN PROGRESS (list following)");
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

                        CALLBACK.followerUpdate(toFollow, "add"+":"+activeUser.getUserName());

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

                        CALLBACK.followerUpdate(toUnfollow, "remove"+":"+activeUser.getUserName());

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

                        for (WinSomePost post : blog.values()) {
                            responseLine.append(post.toString());
                        }

                        // responseLine.append("IN PROGRESS (blog)");

                        break;
                    }

                    case "show": {
                        
                        String subcase = (String)requestLine.nextToken();

                        switch (subcase) {
                            case "feed": {
                                ArrayList<WinSomePost> feed = network.getFeed(activeUser);
        
                                for (WinSomePost post : feed) {
                                    responseLine.append(post.toString());
                                }

                                // responseLine.append("IN PROGRESS (show feed)");
                                
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

                                responseLine.append(post.toString());

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

                            System.out.println("WALLET");
                            WinSomeWallet wallet = activeUser.getWallet();
                            System.out.println(wallet.viewHistory()); 
                            responseLine.append(wallet.viewHistory());
                        }

                        break;
                    }

                    default:
                        break;
                }

                
                Communication.sendResponse(clientOutput, responseLine.toString());
                responseLine.setLength(0); // empty stringbuilder
                 */
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
