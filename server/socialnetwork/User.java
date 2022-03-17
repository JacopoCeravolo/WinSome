package server.socialnetwork;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class User {
    
    private Integer uniqueID;

    private String username;
    private String password;

    private UserStatus status;

    private ArrayList<String> tagsList;
    private TreeSet<String> following;
    private TreeSet<String> followers;

    private TreeSet<Integer> blog;

    private Wallet wallet;
    
    
    // Constructor

    public User(String username, String password, List<String> tagsList){

        this.username = username;
        this.password = password; // TODO: encryption

        this.tagsList = new ArrayList<>(tagsList.size());

        for (String tag : tagsList) {
            this.tagsList.add(tag.toLowerCase());
        }

        
        this.following = new TreeSet<>();
        this.followers = new TreeSet<>();

        this.blog = new TreeSet<>();

        this.wallet = new Wallet();

    }

    public void setUserID(Integer uniqueID) {
        this.uniqueID = uniqueID;
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

    public TreeSet<Integer> getBlog() {
        return this.blog;
    }

    public Collection<String> getTags() {
        return this.tagsList;
    }

    public TreeSet<String> getFollowing() {
        return this.following;
    }

    public TreeSet<String> getFollowers() {
        return this.followers;
    }
    

    public Integer getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(Integer uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public ArrayList<String> getTagsList() {
        return tagsList;
    }

    public void setTagsList(ArrayList<String> tagsList) {
        this.tagsList = tagsList;
    }

    public void setFollowing(TreeSet<String> following) {
        this.following = following;
    }

    public void setFollowers(TreeSet<String> followers) {
        this.followers = followers;
    }

    public void setBlog(TreeSet<Integer> blog) {
        this.blog = blog;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public String toString() {

        // TODO: change
        String userFormatted = String.format(
            "| %10s | %d [followers] | %d [following] | ", username, followers.size(), following.size());
        
        for (String tag : tagsList) {
            userFormatted += tag + " ";
        }

        
        return userFormatted;
    }

}
