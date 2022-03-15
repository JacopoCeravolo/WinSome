package server.socialnetwork;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class User {
    
    private Integer uniqueID;

    private String username;
    private String password;

    private UserStatus status;

    private ArrayList<String> tagsList;
    private HashMap<String, User> followers;
    private HashMap<String, User> following;

    private HashMap<Integer, Post> blog;

    private Wallet wallet;
    
    
    // Constructor

    public User(String username, String password, List<String> tagsList){

        this.username = username;
        this.password = password; // TODO: encryption

        this.tagsList = new ArrayList<>(tagsList.size());

        for (String tag : tagsList) {
            this.tagsList.add(tag.toLowerCase());
        }

        this.followers = new HashMap<>();
        this.following = new HashMap<>();

        this.blog = new HashMap<>();

        this.wallet = new Wallet();

    }

    public void setUserID(Integer uniqueID) {
        this.uniqueID = uniqueID;
    }


    public void followUser(User user) {
        
        following.putIfAbsent(user.getUserName(), user);
        user.getFollowers().putIfAbsent(this.username, this);

    }

    public Wallet getWallet() {
        return this.wallet;
    }
    // Getters

    public Integer getUserID() {
        return this.uniqueID;
    }
    
    public String getUserName() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public UserStatus getUserStatus() {
        return this.status;
    }

    public void setUserStatus(UserStatus status) {
        this.status = status;
    }

    public HashMap<Integer, Post> getBlog() {
        return this.blog;
    }

    public HashMap<String, User> getFollowers() {
        return this.followers;
    }

    public Collection<String> getTags() {
        return this.tagsList;
    }

    public HashMap<String, User> getFollowing() {
        return this.following;
    }

    @Override
    public String toString() {


        String userFormatted = String.format(
            "| %10s | %d [followers] | %d [following] | ", username, followers.size(), following.size());
        
        for (String tag : tagsList) {
            userFormatted += tag + ", ";
        }

        userFormatted += "\n";
        
        return userFormatted;
    }

}
