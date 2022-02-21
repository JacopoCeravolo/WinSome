import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WinSomeUser {
    
    private UUID uniqueID;

    private String username;
    private String password;

    private ArrayList<String> tagsList;
    private HashMap<UUID, WinSomeUser> followers;
    private HashMap<UUID, WinSomeUser> following;

    private WinSomeUserStatus status;
    
    
    // Constructor

    public WinSomeUser(String username, String password, List<String> tagsList){

        this.uniqueID = UUID.randomUUID();
        this.username = username;
        this.password = password; // TODO: encryption

        this.tagsList = new ArrayList<>(tagsList.size());

        for (String tag : tagsList) {
            this.tagsList.add(tag.toLowerCase());
        }

        this.followers = new HashMap<>();
        this.following = new HashMap<>();

        this.status = WinSomeUserStatus.REGISTERED;
    }

    
    // Getters

    public UUID getUserID() {
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

    public Collection<WinSomeUser> getFollowers() {
        return this.followers.values();
    }

    public Collection<WinSomeUser> getFollowing() {
        return this.following.values();
    }

    @Override
    public String toString() {
    
        return new String(username + " : " + status.toString() + " : " + followers + " : " + following);
    }

}
