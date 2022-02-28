package server.socialnetwork;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WinSomeUser {
    
    private Integer uniqueID;

    private String username;
    private String password;

    private WinSomeUserStatus status;

    private ArrayList<String> tagsList;
    private HashMap<String, WinSomeUser> followers;
    private HashMap<String, WinSomeUser> following;

    private HashMap<Integer, WinSomePost> blog;

    private WinSomeWallet wallet;
    
    
    // Constructor

    public WinSomeUser(String username, String password, List<String> tagsList){

        this.username = username;
        this.password = password; // TODO: encryption

        this.tagsList = new ArrayList<>(tagsList.size());

        for (String tag : tagsList) {
            this.tagsList.add(tag.toLowerCase());
        }

        this.followers = new HashMap<>();
        this.following = new HashMap<>();

        this.blog = new HashMap<>();

        this.wallet = new WinSomeWallet();

    }

    public void setUserID(Integer uniqueID) {
        this.uniqueID = uniqueID;
    }


    public void followUser(WinSomeUser user) {
        
        following.putIfAbsent(user.getUserName(), user);
        user.getFollowers().putIfAbsent(this.username, this);

    }

    public WinSomeWallet getWallet() {
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

    public WinSomeUserStatus getUserStatus() {
        return this.status;
    }

    public void setUserStatus(WinSomeUserStatus status) {
        this.status = status;
    }

    public HashMap<Integer, WinSomePost> getBlog() {
        return this.blog;
    }

    public HashMap<String, WinSomeUser> getFollowers() {
        return this.followers;
    }

    public Collection<String> getTags() {
        return this.tagsList;
    }

    public HashMap<String, WinSomeUser> getFollowing() {
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
